const jsonHeaders = {
  'Content-Type': 'application/json'
}

async function request(url, options = {}) {
  const response = await fetch(url, options)
  const payload = await response.json().catch(() => null)

  if (!response.ok || payload?.success === false) {
    const message = payload?.message || 'Request failed.'
    const error = new Error(message)
    error.errorCode = payload?.errorCode || 'REQUEST_ERROR'
    throw error
  }

  return payload?.data ?? payload
}

export function getFloors() {
  return request('/api/floors')
}

export function getSeatsByFloor(floorId) {
  assertPositiveInteger(floorId, 'floorId')
  return request(`/api/floors/${floorId}/seats`)
}

export function getEmployees(keyword = '') {
  const params = new URLSearchParams()
  if (keyword.trim()) {
    params.set('keyword', keyword.trim())
  }

  const query = params.toString()
  return request(`/api/employees${query ? `?${query}` : ''}`)
}

export function createEmployee({ employeeNo, employeeName }) {
  assertEmployeeNo(employeeNo)
  return request('/api/employees', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify({ employeeNo, employeeName })
  })
}

export function deleteEmployee(employeeNo) {
  assertEmployeeNo(employeeNo)
  return request(`/api/employees/${employeeNo}`, { method: 'DELETE' })
}

export function createSeat({ floorId, seatNo }) {
  assertPositiveInteger(floorId, 'floorId')
  return request('/api/seats', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify({ floorId, seatNo })
  })
}

export function deleteSeat(seatId) {
  assertPositiveInteger(seatId, 'seatId')
  return request(`/api/seats/${seatId}`, { method: 'DELETE' })
}

export function addPendingAssignment({ seatId, employeeNo, assignedBy }) {
  assertPositiveInteger(seatId, 'seatId')
  assertEmployeeNo(employeeNo)
  return request('/api/seat-assignments/assign', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify({ seatId, employeeNo, assignedBy })
  })
}

export function addPendingClear({ seatId, releasedBy }) {
  assertPositiveInteger(seatId, 'seatId')
  return request('/api/seat-assignments/clear', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify({ seatId, releasedBy })
  })
}

export function submitSeatAssignments({ submittedBy }) {
  return request('/api/seat-assignments/submit', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify({ submittedBy })
  })
}

export function resetPendingSelections({ submittedBy }) {
  return request('/api/seat-assignments/reset', {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify({ submittedBy })
  })
}

function assertPositiveInteger(value, name) {
  if (!Number.isInteger(Number(value)) || Number(value) <= 0) {
    throw new Error(`${name} must be a positive integer.`)
  }
}

function assertEmployeeNo(employeeNo) {
  if (!/^\d{5}$/.test(employeeNo || '')) {
    throw new Error('employeeNo must be exactly 5 digits.')
  }
}
