// Base configuration
    const ACTUATOR_BASE_URL = '/actuator';
    const REFRESH_INTERVAL = 5000;
    
    let refreshTimer;
    let isOnline = false;

    // Utility functions
    function formatBytes(bytes) {
        if (bytes === 0) return '0 B';
        if (bytes === undefined) return '-';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    function formatDuration(seconds) {
        if (!seconds) return '-';
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        const secs = Math.floor(seconds % 60);
        return `${hours}h ${minutes}m ${secs}s`;
    }

    function formatPercentage(value, max) {
        if (!value || !max) return '0%';
        return ((value / max) * 100).toFixed(1) + '%';
    }

    function updateProgressBar(elementId, percentage) {
        const element = document.getElementById(elementId);
        if (element) {
            // Smooth animation
            setTimeout(() => {
                element.style.width = percentage + '%';
            }, 100);
            
            element.className = 'progress-fill';
            if (percentage > 80) element.classList.add('danger');
            else if (percentage > 60) element.classList.add('warning');
        }
    }

    function showError(message) {
        const errorContainer = document.getElementById('errorContainer');
        errorContainer.innerHTML = `<div class="error-message">⚠️ ${message}</div>`;
    }

    function clearError() {
        document.getElementById('errorContainer').innerHTML = '';
    }

    function updateConnectionStatus(online) {
        isOnline = online;
        const statusDot = document.getElementById('connectionStatus');
        const statusText = document.getElementById('connectionText');
        
        if (online) {
            statusDot.className = 'status-dot';
            statusText.textContent = 'Connected';
            clearError();
        } else {
            statusDot.className = 'status-dot offline';
            statusText.textContent = 'Disconnected';
        }
    }

    // API call functions
    async function fetchActuatorData(endpoint) {
        try {
            const response = await fetch(`${ACTUATOR_BASE_URL}/${endpoint}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error(`Error fetching ${endpoint}:`, error);
            throw error;
        }
    }

    async function updateHealthStatus() {
        try {
            const health = await fetchActuatorData('health');
            const statusElement = document.getElementById('healthStatus');
            const detailsElement = document.getElementById('healthDetails');
            
            statusElement.innerHTML = health.status;
            statusElement.className = `health-status ${health.status.toLowerCase()}`;
            
            let detailsHtml = '';
            if (health.components) {
                Object.entries(health.components).forEach(([key, value]) => {
                    const statusClass = value.status ? value.status.toLowerCase() : 'unknown';
                    detailsHtml += `
                        <div class="metric">
                            <span class="metric-label">
                                <span class="metric-icon">🔧</span>
                                ${key}
                            </span>
                            <span class="health-status ${statusClass}">${value.status || 'Unknown'}</span>
                        </div>
                    `;
                });
            }
            detailsElement.innerHTML = detailsHtml;
        } catch (error) {
            document.getElementById('healthStatus').innerHTML = 'Error';
            document.getElementById('healthStatus').className = 'health-status down';
        }
    }

    async function updateMemoryInfo() {
        try {
            const memoryMetrics = {};
            
            const memoryEndpoints = [
                'jvm.memory.used?tag=area:heap',
                'jvm.memory.max?tag=area:heap',
                'jvm.memory.used?tag=area:nonheap'
            ];
            
            for (let endpoint of memoryEndpoints) {
                try {
                    const metric = await fetchActuatorData(`metrics/${endpoint}`);
                    const key = endpoint.split('?')[0].split('.').pop() + (endpoint.includes('nonheap') ? '_nonheap' : '');
                    memoryMetrics[key] = metric.measurements[0]?.value || 0;
                } catch (e) {
                    console.warn(`Could not fetch ${endpoint}`);
                }
            }
            
            document.getElementById('heapUsed').textContent = formatBytes(memoryMetrics.used);
            document.getElementById('heapMax').textContent = formatBytes(memoryMetrics.max);
            document.getElementById('nonHeapUsed').textContent = formatBytes(memoryMetrics.used_nonheap);
            
            const heapPercentage = (memoryMetrics.used / memoryMetrics.max) * 100;
            updateProgressBar('heapProgress', heapPercentage);
            
        } catch (error) {
            console.error('Error updating memory info:', error);
        }
    }

    async function updateSystemInfo() {
        try {
            const promises = [
                fetchActuatorData('metrics/system.cpu.usage').catch(() => ({ measurements: [{ value: 0 }] })),
                fetchActuatorData('metrics/process.uptime').catch(() => ({ measurements: [{ value: 0 }] })),
                fetchActuatorData('metrics/system.cpu.count').catch(() => ({ measurements: [{ value: 0 }] }))
            ];
            
            const [cpuMetric, uptimeMetric, processorsMetric] = await Promise.all(promises);
            
            const cpuUsage = (cpuMetric.measurements[0]?.value || 0) * 100;
            document.getElementById('cpuUsage').textContent = cpuUsage.toFixed(1) + '%';
            updateProgressBar('cpuProgress', cpuUsage);
            
            document.getElementById('uptime').textContent = formatDuration(uptimeMetric.measurements[0]?.value);
            document.getElementById('processors').textContent = processorsMetric.measurements[0]?.value || '-';
            
        } catch (error) {
            console.error('Error updating system info:', error);
        }
    }

    async function updateHttpMetrics() {
        try {
            const httpMetrics = await fetchActuatorData('metrics/http.server.requests').catch(() => null);
            
            if (httpMetrics) {
                let total = 0, success = 0, clientError = 0, serverError = 0;
                
                httpMetrics.availableTags?.forEach(tag => {
                    if (tag.tag === 'status') {
                        tag.values.forEach(status => {
                            const code = parseInt(status);
                            if (code >= 200 && code < 300) success++;
                            else if (code >= 400 && code < 500) clientError++;
                            else if (code >= 500) serverError++;
                            total++;
                        });
                    }
                });
                
                document.getElementById('httpTotal').textContent = total || '-';
                document.getElementById('http2xx').textContent = success || '-';
                document.getElementById('http4xx').textContent = clientError || '-';
                document.getElementById('http5xx').textContent = serverError || '-';
            } else {
                document.getElementById('httpTotal').textContent = '-';
                document.getElementById('http2xx').textContent = '-';
                document.getElementById('http4xx').textContent = '-';
                document.getElementById('http5xx').textContent = '-';
            }
        } catch (error) {
            console.error('Error updating HTTP metrics:', error);
        }
    }

    async function updateThreadInfo() {
        try {
            const promises = [
                fetchActuatorData('metrics/jvm.threads.live').catch(() => ({ measurements: [{ value: 0 }] })),
                fetchActuatorData('metrics/jvm.threads.peak').catch(() => ({ measurements: [{ value: 0 }] })),
                fetchActuatorData('metrics/jvm.threads.daemon').catch(() => ({ measurements: [{ value: 0 }] }))
            ];
            
            const [liveThreads, peakThreads, daemonThreads] = await Promise.all(promises);
            
            document.getElementById('threadCount').textContent = liveThreads.measurements[0]?.value || '-';
            document.getElementById('peakThreadCount').textContent = peakThreads.measurements[0]?.value || '-';
            document.getElementById('daemonThreadCount').textContent = daemonThreads.measurements[0]?.value || '-';
            
        } catch (error) {
            console.error('Error updating thread info:', error);
        }
    }

    async function updateGcInfo() {
        try {
            const gcMetrics = await fetchActuatorData('metrics').catch(() => ({ names: [] }));
            const gcElement = document.getElementById('gcInfo');
            
            const gcNames = gcMetrics.names?.filter(name => name.includes('jvm.gc.')) || [];
            
            if (gcNames.length > 0) {
                let gcHtml = '';
                const mainGcMetrics = gcNames.filter(name => 
                    name.includes('jvm.gc.memory.allocated') || 
                    name.includes('jvm.gc.max.data.size')
                ).slice(0, 3);
                
                for (let metricName of mainGcMetrics) {
                    try {
                        const metric = await fetchActuatorData(`metrics/${metricName}`);
                        const value = metric.measurements[0]?.value || 0;
                        const displayName = metricName.replace('jvm.gc.', '').replace('.', ' ');
                        gcHtml += `
                            <div class="metric">
                                <span class="metric-label">
                                    <span class="metric-icon">♻️</span>
                                    ${displayName}
                                </span>
                                <span class="metric-value">${formatBytes(value)}</span>
                            </div>
                        `;
                    } catch (e) {
                        console.warn(`Could not fetch ${metricName}`);
                    }
                }
                
                gcElement.innerHTML = gcHtml || '<div class="metric"><span class="metric-label"><span class="metric-icon">♻️</span>GC Info</span><span class="metric-value">Not available</span></div>';
            } else {
                gcElement.innerHTML = '<div class="metric"><span class="metric-label"><span class="metric-icon">♻️</span>GC Info</span><span class="metric-value">Not available</span></div>';
            }
        } catch (error) {
            document.getElementById('gcInfo').innerHTML = '<div class="metric"><span class="metric-label"><span class="metric-icon">♻️</span>GC Info</span><span class="metric-value">Error</span></div>';
        }
    }

    // Main refresh function
    async function refreshData() {
        const refreshBtn = document.getElementById('refreshBtn');
        const refreshText = document.getElementById('refreshText');
        
        refreshBtn.disabled = true;
        
        try {
            await Promise.all([
                updateHealthStatus(),
                updateMemoryInfo(),
                updateSystemInfo(),
                updateHttpMetrics(),
                updateThreadInfo(),
                updateGcInfo()
            ]);
            
            updateConnectionStatus(true);
        } catch (error) {
            updateConnectionStatus(false);
            showError('Unable to connect to Actuator endpoints. Please verify that Spring Boot Actuator is configured correctly.');
            console.error('Error refreshing data:', error);
        } finally {
            refreshBtn.disabled = false;
            refreshText.textContent = 'Refresh';
        }
    }

    // Auto refresh
    function startAutoRefresh() {
        refreshTimer = setInterval(() => {
            if (isOnline || document.visibilityState === 'visible') {
                refreshData();
            }
        }, REFRESH_INTERVAL);
    }

    function stopAutoRefresh() {
        if (refreshTimer) {
            clearInterval(refreshTimer);
        }
    }

    // Event listeners
    document.addEventListener('visibilitychange', () => {
        if (document.visibilityState === 'visible') {
            refreshData();
            startAutoRefresh();
        } else {
            stopAutoRefresh();
        }
    });

    // Initialize dashboard
    document.addEventListener('DOMContentLoaded', () => {
        refreshData();
        startAutoRefresh();
    });

    // Cleanup on page unload
    window.addEventListener('beforeunload', () => {
        stopAutoRefresh();
    });