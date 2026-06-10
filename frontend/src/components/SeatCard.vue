<script setup>
const props = defineProps({
  seat: {
    type: Object,
    required: true
  },
  pendingAssignment: {
    type: Object,
    default: null
  },
  pendingClear: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select-seat', 'clear-seat'])

function handleClick() {
  if (props.pendingAssignment || props.pendingClear) {
    emit('clear-seat', props.seat)
    return
  }

  if (props.seat.seatStatus === 'AVAILABLE') {
    emit('select-seat', props.seat)
    return
  }

  if (props.seat.employeeNo) {
    emit('clear-seat', props.seat)
  }
}
</script>

<template>
  <button
    class="seat-card"
    :class="{
      'seat-card--available': seat.seatStatus === 'AVAILABLE' && !pendingAssignment,
      'seat-card--occupied': seat.seatStatus === 'OCCUPIED' && !pendingClear,
      'seat-card--disabled': seat.seatStatus === 'DISABLED',
      'seat-card--pending': pendingAssignment,
      'seat-card--clearing': pendingClear
    }"
    type="button"
    :disabled="seat.seatStatus === 'DISABLED'"
    @click="handleClick"
  >
    <span class="seat-card__no">{{ seat.seatNo }}</span>
    <span v-if="pendingAssignment" class="seat-card__employee">
      {{ pendingAssignment.employeeNo }}
    </span>
    <span v-else-if="pendingClear" class="seat-card__employee">Clearing</span>
    <span v-else-if="seat.employeeNo" class="seat-card__employee">
      {{ seat.employeeNo }}
    </span>
    <span v-else class="seat-card__employee">Open</span>
  </button>
</template>
