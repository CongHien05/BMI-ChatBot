// Admin Authentication Helper
// Tự động thêm JWT token vào header cho mọi request đến /admin/**

(function() {
    'use strict';

    // Lấy token từ localStorage
    function getToken() {
        return localStorage.getItem('adminToken');
    }

    // Kiểm tra xem có phải trang admin không (trừ login)
    function isAdminPage() {
        const path = window.location.pathname;
        return path.startsWith('/admin') && !path.includes('/login');
    }

    // Kiểm tra xem user có role ADMIN không (từ token)
    function hasAdminRole() {
        const token = getToken();
        if (!token) return false;
        
        try {
            // Decode JWT token (chỉ lấy payload, không verify)
            const payload = JSON.parse(atob(token.split('.')[1]));
            // Kiểm tra authorities trong token (nếu có)
            // Hoặc có thể gọi API để verify
            return true; // Tạm thời return true nếu có token
        } catch (e) {
            return false;
        }
    }

    // Redirect về login nếu chưa đăng nhập
    function redirectToLogin() {
        localStorage.removeItem('adminToken');
        localStorage.removeItem('adminEmail');
        window.location.href = '/admin/login';
    }

    // Thêm token vào header cho XMLHttpRequest và fetch
    function setupTokenInterceptor() {
        // Intercept XMLHttpRequest
        const originalOpen = XMLHttpRequest.prototype.open;
        XMLHttpRequest.prototype.open = function(method, url, ...args) {
            this._url = url;
            return originalOpen.apply(this, [method, url, ...args]);
        };

        const originalSend = XMLHttpRequest.prototype.send;
        XMLHttpRequest.prototype.send = function(...args) {
            if (this._url && this._url.startsWith('/admin')) {
                const token = getToken();
                if (token) {
                    this.setRequestHeader('Authorization', 'Bearer ' + token);
                }
            }
            return originalSend.apply(this, args);
        };

        // Intercept fetch
        const originalFetch = window.fetch;
        window.fetch = function(url, options = {}) {
            if (typeof url === 'string' && url.startsWith('/admin')) {
                const token = getToken();
                if (token) {
                    options.headers = options.headers || {};
                    options.headers['Authorization'] = 'Bearer ' + token;
                }
            }
            return originalFetch(url, options);
        };
    }

    // Xử lý form submit - thêm token vào header
    function setupFormInterceptor() {
        document.addEventListener('submit', function(e) {
            const form = e.target;
            if (form.action && form.action.includes('/admin')) {
                const token = getToken();
                if (token) {
                    // Tạo một input hidden để lưu token (hoặc dùng fetch thay vì form submit)
                    // Nhưng với form submit thông thường, ta cần dùng fetch
                    e.preventDefault();
                    
                    const formData = new FormData(form);
                    const data = Object.fromEntries(formData);
                    
                    fetch(form.action, {
                        method: form.method || 'POST',
                        headers: {
                            'Authorization': 'Bearer ' + token,
                            'Content-Type': 'application/x-www-form-urlencoded'
                        },
                        body: new URLSearchParams(data).toString()
                    })
                    .then(response => {
                        if (response.redirected) {
                            window.location.href = response.url;
                        } else {
                            window.location.reload();
                        }
                    })
                    .catch(error => {
                        console.error('Form submit error:', error);
                        alert('Có lỗi xảy ra. Vui lòng thử lại.');
                    });
                }
            }
        });
    }

    // Kiểm tra authentication khi load trang
    function checkAuth() {
        if (isAdminPage()) {
            const token = getToken();
            if (!token) {
                redirectToLogin();
                return;
            }
            
            // Có thể verify token bằng cách gọi API
            // Tạm thời chỉ kiểm tra có token hay không
        }
    }

    // Khởi tạo
    function init() {
        setupTokenInterceptor();
        setupFormInterceptor();
        checkAuth();
    }

    // Chạy khi DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();

