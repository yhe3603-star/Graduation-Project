import { describe, it, expect, vi, beforeEach } from 'vitest'
import { cancelAllRequests, cancelRequestByUrl } from '@/utils/request'

describe('request - pending cancellation', () => {
  beforeEach(() => {
    // Reset module state
    cancelAllRequests()
  })

  it('cancelAllRequests should not throw', () => {
    expect(() => cancelAllRequests()).not.toThrow()
  })

  it('cancelRequestByUrl should not throw with any url', () => {
    expect(() => cancelRequestByUrl('/api/some-endpoint')).not.toThrow()
  })

  it('cancelRequestByUrl should not throw with empty url', () => {
    expect(() => cancelRequestByUrl('')).not.toThrow()
  })
})
