import { throttle, debounce } from 'lodash-es'

export const vLazy = {
  mounted(el, binding) {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const src = binding.value || el.dataset.src
            if (src) {
              el.src = src
              el.classList.remove('lazy-loading')
              el.classList.add('lazy-loaded')
            }
            observer.unobserve(el)
          }
        })
      },
      {
        rootMargin: '50px',
        threshold: 0.1
      }
    )
    
    el.classList.add('lazy-loading')
    el._observer = observer
    observer.observe(el)
  },
  
  unmounted(el) {
    if (el._observer) {
      el._observer.disconnect()
    }
  }
}

export const vLazyBackground = {
  mounted(el, binding) {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const src = binding.value || el.dataset.bgSrc
            if (src) {
              el.style.backgroundImage = `url(${src})`
              el.classList.remove('lazy-loading')
              el.classList.add('lazy-loaded')
            }
            observer.unobserve(el)
          }
        })
      },
      {
        rootMargin: '50px',
        threshold: 0.1
      }
    )
    
    el.classList.add('lazy-loading')
    el._observer = observer
    observer.observe(el)
  },
  
  unmounted(el) {
    if (el._observer) {
      el._observer.disconnect()
    }
  }
}

export const vDebounce = {
  mounted(el, binding) {
    const { value, arg = 'click' } = binding
    const delay = typeof value === 'number' ? value : 300
    const handler = typeof value === 'function' ? value : binding.value?.handler
    
    if (handler) {
      el._debounceHandler = debounce(handler, delay)
      el.addEventListener(arg, el._debounceHandler)
    }
  },
  
  unmounted(el, binding) {
    const { arg = 'click' } = binding
    if (el._debounceHandler) {
      el.removeEventListener(arg, el._debounceHandler)
      el._debounceHandler.cancel()
    }
  }
}

export const vThrottle = {
  mounted(el, binding) {
    const { value, arg = 'click' } = binding
    const delay = typeof value === 'number' ? value : 300
    const handler = typeof value === 'function' ? value : binding.value?.handler
    
    if (handler) {
      el._throttleHandler = throttle(handler, delay)
      el.addEventListener(arg, el._throttleHandler)
    }
  },
  
  unmounted(el, binding) {
    const { arg = 'click' } = binding
    if (el._throttleHandler) {
      el.removeEventListener(arg, el._throttleHandler)
      el._throttleHandler.cancel()
    }
  }
}

export const vClickOutside = {
  mounted(el, binding) {
    el._clickOutsideHandler = (event) => {
      if (!el.contains(event.target) && el !== event.target) {
        binding.value(event)
      }
    }
    document.addEventListener('click', el._clickOutsideHandler)
  },
  
  unmounted(el) {
    if (el._clickOutsideHandler) {
      document.removeEventListener('click', el._clickOutsideHandler)
    }
  }
}

export const vFocus = {
  mounted(el) {
    el.focus()
  }
}

export const vPermission = {
  mounted(el, binding) {
    const { value } = binding
    const role = localStorage.getItem('role') || 'user'
    const requiredRoles = Array.isArray(value) ? value : [value]

    if (value && !requiredRoles.includes(role) && role.toUpperCase() !== 'ADMIN') {
      el.parentNode?.removeChild(el)
    }
  }
}

function createLoadingElement() {
  const loadingEl = document.createElement('div')
  loadingEl.className = 'loading-spinner'
  const spinner = document.createElement('div')
  spinner.className = 'spinner'
  loadingEl.appendChild(spinner)
  return loadingEl
}

export const vLoading = {
  mounted(el, binding) {
    if (binding.value) {
      el.classList.add('loading')
      const loadingEl = createLoadingElement()
      el.appendChild(loadingEl)
      el._loadingEl = loadingEl
    }
  },

  updated(el, binding) {
    if (binding.value !== binding.oldValue) {
      if (binding.value) {
        el.classList.add('loading')
        if (!el._loadingEl) {
          const loadingEl = createLoadingElement()
          el.appendChild(loadingEl)
          el._loadingEl = loadingEl
        }
      } else {
        el.classList.remove('loading')
        if (el._loadingEl) {
          el._loadingEl.remove()
          el._loadingEl = null
        }
      }
    }
  }
}

export const directives = {
  install(app) {
    app.directive('lazy', vLazy)
    app.directive('lazy-background', vLazyBackground)
    app.directive('debounce', vDebounce)
    app.directive('throttle', vThrottle)
    app.directive('click-outside', vClickOutside)
    app.directive('focus', vFocus)
    app.directive('permission', vPermission)
    app.directive('loading', vLoading)
  }
}

export default directives
