<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import SeatMap from './components/SeatMap.vue'
import ManagementPanel from './components/ManagementPanel.vue'
import {
  addPendingAssignment,
  addPendingClear,
  getEmployees,
  getFloors,
  getSeatsByFloor,
  resetPendingSelections,
  submitSeatAssignments
} from './api/seatingApi'

const operator = ref('admin')
const floors = ref([])
const employees = ref([])
const seats = ref([])
const allSeats = ref([])
const selectedFloorId = ref('')
const selectedEmployeeNo = ref('')
const loading = ref(false)
const submitting = ref(false)
const resetting = ref(false)
const message = ref('')
const error = ref('')
const errorCode = ref('')

const pendingAssignments = reactive({})
const pendingAssignmentsByEmployee = reactive({})
const pendingClears = reactive({})

const selectedEmployee = computed(() => {
  return employees.value.find((employee) => employee.employeeNo === selectedEmployeeNo.value)
})

const hasPendingChanges = computed(() => {
  return Object.keys(pendingAssignments).length > 0 || Object.keys(pendingClears).length > 0
})

const sortedSeats = computed(() => {
  return [...seats.value].sort((a, b) => a.seatNo.localeCompare(b.seatNo))
})

const occupiedEmployeeNos = computed(() => {
  const occupied = new Set()
  allSeats.value.forEach((seat) => {
    if (seat.employeeNo) {
      occupied.add(seat.employeeNo)
    }
  })
  return Array.from(occupied)
})

const deletableSeats = computed(() => {
  return sortedSeats.value.filter(
    (seat) =>
      !seat.employeeNo &&
      seat.seatStatus === 'AVAILABLE' &&
      !pendingAssignments[seat.seatId]
  )
})

async function loadAllSeats() {
  if (floors.value.length === 0) {
    allSeats.value = []
    return
  }

  const seatGroups = await Promise.all(
    floors.value.map((floor) => getSeatsByFloor(floor.floorId))
  )
  allSeats.value = seatGroups.flat()
}

async function loadInitialData() {
  loading.value = true
  clearStatus()

  try {
    const [floorData, employeeData] = await Promise.all([getFloors(), getEmployees()])
    floors.value = floorData
    employees.value = employeeData

    if (floorData.length > 0) {
      selectedFloorId.value = floorData[0].floorId
      await loadSeats()
      await loadAllSeats()
    }
  } catch (err) {
    setError(err)
  } finally {
    loading.value = false
  }
}

async function loadSeats() {
  if (!selectedFloorId.value) return

  loading.value = true
  clearStatus()

  try {
    seats.value = await getSeatsByFloor(selectedFloorId.value)
    await loadAllSeats()
  } catch (err) {
    setError(err)
  } finally {
    loading.value = false
  }
}

async function handleSelectSeat(seat) {
  clearStatus()

  if (!selectedEmployee.value) {
    setError(new Error('Select an employee before choosing a seat.'))
    return
  }

  if (pendingAssignmentsByEmployee[selectedEmployee.value.employeeNo]) {
    setError(new Error('This employee already has a pending seat.'))
    return
  }

  try {
    await addPendingAssignment({
      seatId: seat.seatId,
      employeeNo: selectedEmployee.value.employeeNo,
      assignedBy: operator.value
    })

    pendingAssignments[seat.seatId] = {
      seatId: seat.seatId,
      employeeNo: selectedEmployee.value.employeeNo,
      employeeName: selectedEmployee.value.employeeName
    }
    pendingAssignmentsByEmployee[selectedEmployee.value.employeeNo] = seat.seatId
    message.value = 'Pending assignment added.'
  } catch (err) {
    setError(err)
  }
}

async function handleClearSeat(seat) {
  clearStatus()

  try {
    await addPendingClear({
      seatId: seat.seatId,
      releasedBy: operator.value
    })

    if (pendingAssignments[seat.seatId]) {
      const employeeNo = pendingAssignments[seat.seatId].employeeNo
      delete pendingAssignments[seat.seatId]
      delete pendingAssignmentsByEmployee[employeeNo]
      message.value = 'Pending assignment removed.'
      return
    }

    if (pendingClears[seat.seatId]) {
      delete pendingClears[seat.seatId]
      message.value = 'Pending clear removed.'
      return
    }

    if (!seat.employeeNo) {
      setError(new Error('This seat is already open.'))
      return
    }

    pendingClears[seat.seatId] = true
    message.value = 'Pending clear added.'
  } catch (err) {
    setError(err)
  }
}

async function submitChanges() {
  submitting.value = true
  clearStatus()

  try {
    const result = await submitSeatAssignments({ submittedBy: operator.value })
    clearPendingState()
    await loadSeats()
    message.value = `Submitted ${result.assignedCount} assignment(s), cleared ${result.clearedCount} seat(s).`
  } catch (err) {
    setError(err)
  } finally {
    submitting.value = false
  }
}

async function resetSelections() {
  resetting.value = true
  clearStatus()

  try {
    await resetPendingSelections({ submittedBy: operator.value })
    clearPendingState()
    selectedEmployeeNo.value = ''
    message.value = 'All selections reset.'
  } catch (err) {
    setError(err)
  } finally {
    resetting.value = false
  }
}

async function handleEmployeeCreated() {
  employees.value = await getEmployees()
  await loadSeats()
}

async function handleEmployeeDeleted(employeeNo) {
  if (selectedEmployeeNo.value === employeeNo) {
    selectedEmployeeNo.value = ''
  }
  Object.entries(pendingAssignments).forEach(([seatId, assignment]) => {
    if (assignment.employeeNo === employeeNo) {
      delete pendingAssignments[seatId]
    }
  })
  delete pendingAssignmentsByEmployee[employeeNo]
  employees.value = await getEmployees()
  await loadAllSeats()
}

async function handleSeatCreated() {
  await loadSeats()
}

async function handleSeatDeleted(seatId) {
  delete pendingAssignments[seatId]
  delete pendingClears[seatId]
  Object.entries(pendingAssignmentsByEmployee).forEach(([employeeNo, assignedSeatId]) => {
    if (assignedSeatId === seatId) {
      delete pendingAssignmentsByEmployee[employeeNo]
    }
  })
  await loadSeats()
}

function clearStatus() {
  error.value = ''
  errorCode.value = ''
  message.value = ''
}

function setError(err) {
  error.value = sanitizeMessage(err?.message || 'Request failed.')
  errorCode.value = sanitizeMessage(err?.errorCode || '')
}

function sanitizeMessage(value) {
  return String(value).replace(/[\u0000-\u001f\u007f<>]/g, '').slice(0, 180)
}

function clearPendingState() {
  Object.keys(pendingAssignments).forEach((seatId) => delete pendingAssignments[seatId])
  Object.keys(pendingAssignmentsByEmployee).forEach((employeeNo) => delete pendingAssignmentsByEmployee[employeeNo])
  Object.keys(pendingClears).forEach((seatId) => delete pendingClears[seatId])
}

onMounted(loadInitialData)
</script>

<template>
  <main class="page-shell">
    <h1>Employee Seating Arrangement System</h1>

    <section class="toolbar">
      <div class="field">
        <label for="floor">Floor</label>
        <select id="floor" v-model="selectedFloorId" @change="loadSeats">
          <option v-for="floor in floors" :key="floor.floorId" :value="floor.floorId">
            {{ floor.floorName }}
          </option>
        </select>
      </div>

      <div class="field field--wide">
        <label for="employee">Employee</label>
        <select id="employee" v-model="selectedEmployeeNo">
          <option value="">Select employee</option>
          <option v-for="employee in employees" :key="employee.employeeNo" :value="employee.employeeNo">
            {{ employee.employeeNo }} - {{ employee.employeeName }}
          </option>
        </select>
      </div>

      <div class="toolbar__actions">
        <button
          class="reset-button"
          type="button"
          :disabled="resetting || (!hasPendingChanges && !selectedEmployeeNo)"
          @click="resetSelections"
        >
          {{ resetting ? 'Resetting' : 'Reset' }}
        </button>
        <button
          class="submit-button"
          type="button"
          :disabled="!hasPendingChanges || submitting"
          @click="submitChanges"
        >
          {{ submitting ? 'Submitting' : 'Submit' }}
        </button>
      </div>
    </section>

    <section class="status-row" aria-live="polite">
      <span v-if="loading">Loading seats...</span>
      <span v-else-if="error" class="status-row__error">
        <strong v-if="errorCode">{{ errorCode }}:</strong> {{ error }}
      </span>
      <span v-else-if="message" class="status-row__success">{{ message }}</span>
      <span v-else>
        {{ Object.keys(pendingAssignments).length }} pending,
        {{ Object.keys(pendingClears).length }} clearing
      </span>
    </section>

    <SeatMap
      :seats="sortedSeats"
      :pending-assignments="pendingAssignments"
      :pending-clears="pendingClears"
      @select-seat="handleSelectSeat"
      @clear-seat="handleClearSeat"
    />

    <ManagementPanel
      :floors="floors"
      :employees="employees"
      :deletable-seats="deletableSeats"
      :occupied-employee-nos="occupiedEmployeeNos"
      @employee-created="handleEmployeeCreated"
      @employee-deleted="handleEmployeeDeleted"
      @seat-created="handleSeatCreated"
      @seat-deleted="handleSeatDeleted"
      @error="setError"
      @success="message = $event"
    />
  </main>
</template>
