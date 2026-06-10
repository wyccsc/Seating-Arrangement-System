<script setup>
import { computed, ref } from 'vue'
import { createEmployee, createSeat, deleteEmployee, deleteSeat } from '../api/seatingApi'

const props = defineProps({
  floors: {
    type: Array,
    required: true
  },
  employees: {
    type: Array,
    required: true
  },
  deletableSeats: {
    type: Array,
    required: true
  },
  occupiedEmployeeNos: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits([
  'employee-created',
  'employee-deleted',
  'seat-created',
  'seat-deleted',
  'error',
  'success'
])

const showEmployeeForm = ref(false)
const showSeatForm = ref(false)
const showDeleteSection = ref(false)

const employeeForm = ref({
  employeeNo: '',
  employeeName: ''
})

const seatForm = ref({
  floorId: '',
  seatNo: ''
})

const deleteEmployeeNo = ref('')
const deleteSeatId = ref('')
const loading = ref(false)

const deletableEmployees = computed(() => {
  return props.employees.filter((employee) => !props.occupiedEmployeeNos.includes(employee.employeeNo))
})

const selectedDeleteEmployee = computed(() => {
  return deletableEmployees.value.find((employee) => employee.employeeNo === deleteEmployeeNo.value)
})

const selectedDeleteSeat = computed(() => {
  return props.deletableSeats.find((seat) => seat.seatId === Number(deleteSeatId.value))
})

async function handleAddEmployee() {
  if (!employeeForm.value.employeeNo || !employeeForm.value.employeeName) {
    emit('error', new Error('Please fill in all required fields'))
    return
  }

  loading.value = true
  try {
    await createEmployee({
      employeeNo: employeeForm.value.employeeNo,
      employeeName: employeeForm.value.employeeName
    })

    emit('success', 'Employee created successfully')
    emit('employee-created')

    employeeForm.value = { employeeNo: '', employeeName: '' }
    showEmployeeForm.value = false
  } catch (err) {
    emit('error', err)
  } finally {
    loading.value = false
  }
}

async function handleAddSeat() {
  if (!seatForm.value.floorId || !seatForm.value.seatNo) {
    emit('error', new Error('Please fill in all required fields'))
    return
  }

  loading.value = true
  try {
    await createSeat({
      floorId: parseInt(seatForm.value.floorId),
      seatNo: seatForm.value.seatNo
    })

    emit('success', 'Seat created successfully')
    emit('seat-created')

    seatForm.value = { floorId: '', seatNo: '' }
    showSeatForm.value = false
  } catch (err) {
    emit('error', err)
  } finally {
    loading.value = false
  }
}

async function handleDeleteEmployee() {
  if (!selectedDeleteEmployee.value) {
    emit('error', new Error('Select an employee without a seat to delete.'))
    return
  }

  const employee = selectedDeleteEmployee.value
  if (!confirm(`Delete employee ${employee.employeeNo} - ${employee.employeeName}?`)) {
    return
  }

  loading.value = true
  try {
    await deleteEmployee(employee.employeeNo)
    emit('success', 'Employee deleted successfully')
    emit('employee-deleted', employee.employeeNo)
    deleteEmployeeNo.value = ''
  } catch (err) {
    emit('error', err)
  } finally {
    loading.value = false
  }
}

async function handleDeleteSeat() {
  if (!selectedDeleteSeat.value) {
    emit('error', new Error('Select an open seat to delete.'))
    return
  }

  const seat = selectedDeleteSeat.value
  if (!confirm(`Delete seat ${seat.seatNo}?`)) {
    return
  }

  loading.value = true
  try {
    await deleteSeat(seat.seatId)
    emit('success', 'Seat deleted successfully')
    emit('seat-deleted', seat.seatId)
    deleteSeatId.value = ''
  } catch (err) {
    emit('error', err)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="management-panel">
    <h2 class="management-panel__title">Management</h2>

    <div class="button-group">
      <button
        type="button"
        class="btn-management"
        @click="showEmployeeForm = !showEmployeeForm"
      >
        {{ showEmployeeForm ? 'Cancel' : 'Add Employee' }}
      </button>
      <button
        type="button"
        class="btn-management"
        @click="showSeatForm = !showSeatForm"
      >
        {{ showSeatForm ? 'Cancel' : 'Add Seat' }}
      </button>
      <button
        type="button"
        class="btn-management btn-management--danger"
        @click="showDeleteSection = !showDeleteSection"
      >
        {{ showDeleteSection ? 'Cancel Delete' : 'Delete' }}
      </button>
    </div>

    <div v-if="showEmployeeForm" class="form-container">
      <h3>Add New Employee</h3>
      <form @submit.prevent="handleAddEmployee">
        <div class="form-group">
          <label for="emp-no">Employee No (5 digits)</label>
          <input
            id="emp-no"
            v-model="employeeForm.employeeNo"
            type="text"
            placeholder="00001"
            maxlength="5"
            required
          />
        </div>
        <div class="form-group">
          <label for="emp-name">Employee Name</label>
          <input
            id="emp-name"
            v-model="employeeForm.employeeName"
            type="text"
            placeholder="John Doe"
            required
          />
        </div>
        <button type="submit" class="btn-submit" :disabled="loading">
          {{ loading ? 'Creating...' : 'Create Employee' }}
        </button>
      </form>
    </div>

    <div v-if="showSeatForm" class="form-container">
      <h3>Add New Seat</h3>
      <form @submit.prevent="handleAddSeat">
        <div class="form-group">
          <label for="seat-floor">Floor</label>
          <select id="seat-floor" v-model="seatForm.floorId" required>
            <option value="">Select floor</option>
            <option v-for="floor in floors" :key="floor.floorId" :value="floor.floorId">
              {{ floor.floorName }}
            </option>
          </select>
        </div>
        <div class="form-group">
          <label for="seat-no">Seat No</label>
          <input
            id="seat-no"
            v-model="seatForm.seatNo"
            type="text"
            placeholder="A-01"
            required
          />
        </div>
        <button type="submit" class="btn-submit" :disabled="loading">
          {{ loading ? 'Creating...' : 'Create Seat' }}
        </button>
      </form>
    </div>

    <div v-if="showDeleteSection" class="form-container form-container--delete">
      <h3>Delete</h3>

      <div class="delete-row">
        <div class="form-group">
          <label for="delete-employee">Employee (no seat only)</label>
          <select id="delete-employee" v-model="deleteEmployeeNo">
            <option value="">Select employee</option>
            <option
              v-for="employee in deletableEmployees"
              :key="employee.employeeNo"
              :value="employee.employeeNo"
            >
              {{ employee.employeeNo }} - {{ employee.employeeName }}
            </option>
          </select>
          <p v-if="deletableEmployees.length === 0" class="form-hint">
            No deletable employees. Employees with seats cannot be deleted.
          </p>
        </div>
        <button
          type="button"
          class="btn-delete"
          :disabled="loading || !selectedDeleteEmployee"
          @click="handleDeleteEmployee"
        >
          Delete Employee
        </button>
      </div>

      <div class="delete-row">
        <div class="form-group">
          <label for="delete-seat">Seat on current floor (open only)</label>
          <select id="delete-seat" v-model="deleteSeatId">
            <option value="">Select seat</option>
            <option
              v-for="seat in props.deletableSeats"
              :key="seat.seatId"
              :value="seat.seatId"
            >
              {{ seat.seatNo }}
            </option>
          </select>
          <p v-if="props.deletableSeats.length === 0" class="form-hint">
            No deletable seats on this floor. Occupied seats cannot be deleted.
          </p>
        </div>
        <button
          type="button"
          class="btn-delete"
          :disabled="loading || !selectedDeleteSeat"
          @click="handleDeleteSeat"
        >
          Delete Seat
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.management-panel {
  margin-top: 32px;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px solid #ddd;
}

.management-panel__title {
  margin: 0 0 16px;
  font-size: 18px;
  color: #333;
}

.button-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}

.btn-management {
  padding: 8px 16px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-management:hover {
  background-color: #0056b3;
}

.btn-management--danger {
  background-color: #6c757d;
}

.btn-management--danger:hover {
  background-color: #545b62;
}

.form-container {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #ddd;
  margin-top: 10px;
}

.form-container--delete {
  display: grid;
  gap: 16px;
}

.form-container h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
}

.form-group {
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.form-group label {
  margin-bottom: 5px;
  font-weight: bold;
  font-size: 14px;
  color: #555;
}

.form-group input,
.form-group select {
  padding: 8px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 5px rgba(0, 123, 255, 0.25);
}

.form-hint {
  margin: 6px 0 0;
  color: #888;
  font-size: 13px;
}

.delete-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  align-items: end;
}

.btn-submit {
  padding: 10px 20px;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
}

.btn-submit:hover:not(:disabled) {
  background-color: #218838;
}

.btn-submit:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.btn-delete {
  min-height: 38px;
  padding: 8px 16px;
  background-color: #dc3545;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  white-space: nowrap;
}

.btn-delete:hover:not(:disabled) {
  background-color: #c82333;
}

.btn-delete:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
</style>
