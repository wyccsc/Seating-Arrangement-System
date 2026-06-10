<script setup>
import SeatCard from './SeatCard.vue'

defineProps({
  seats: {
    type: Array,
    required: true
  },
  pendingAssignments: {
    type: Object,
    required: true
  },
  pendingClears: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['select-seat', 'clear-seat'])
</script>

<template>
  <div class="seat-map" aria-label="Seat map">
    <SeatCard
      v-for="seat in seats"
      :key="seat.seatId"
      :seat="seat"
      :pending-assignment="pendingAssignments[seat.seatId]"
      :pending-clear="Boolean(pendingClears[seat.seatId])"
      @select-seat="emit('select-seat', $event)"
      @clear-seat="emit('clear-seat', $event)"
    />
  </div>
</template>
