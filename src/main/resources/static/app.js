// Configuration - 改为使用本地Spring Boot API
const config = {
    baseUrl: '/api',  // 使用本地Spring Boot后端
    username: '',
    instanceName: '',
    pageSize: 100
};

// Storage helper - 改为使用Session而非LocalStorage
const storage = {
    async get(key) {
        try {
            const response = await fetch(`/api/config/get?key=${encodeURIComponent(key)}`, {
                credentials: 'include'
            });
            const result = await response.json();
            return result.status === 0 ? result.data : null;
        } catch (error) {
            console.error('Get config error:', error);
            return null;
        }
    },
    async set(key, value) {
        try {
            const formData = new URLSearchParams();
            formData.append('key', key);
            formData.append('value', value);
            await fetch('/api/config/save', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                credentials: 'include',
                body: formData.toString()
            });
        } catch (error) {
            console.error('Set config error:', error);
        }
    }
};

// HTTP utilities - 简化版，直接使用fetch
const http = {
    async get(url) {
        const response = await fetch(url, {
            method: 'GET',
            credentials: 'include'
        });
        return await response.json();
    },

    async post(url, data) {
        const formData = new URLSearchParams();
        for (const key in data) {
            formData.append(key, data[key]);
        }

        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
            },
            credentials: 'include',
            body: formData.toString()
        });
        return await response.json();
    }
};

// API functions
const api = {
    async login(username, password) {
        try {
            const result = await http.post(`${config.baseUrl}/authenticate`, {
                username,
                password
            });
            
            if (result.status === 0) {
                config.username = username;
                await storage.set('username', username);
                return true;
            }
            return false;
        } catch (error) {
            console.error('Login error:', error);
            return false;
        }
    },

    async queryServers() {
        try {
            const result = await http.get(`${config.baseUrl}/servers`);
            return result.data ? result.data.map(item => item.instance_name) : [];
        } catch (error) {
            console.error('Query servers error:', error);
            return null;
        }
    },

    async queryDatabases(instanceName) {
        try {
            const result = await http.get(
                `${config.baseUrl}/databases?instanceName=${encodeURIComponent(instanceName)}`
            );
            return result.data || [];
        } catch (error) {
            console.error('Query databases error:', error);
            return [];
        }
    },

    async queryTables(instanceName, dbName) {
        try {
            const result = await http.get(
                `${config.baseUrl}/tables?instanceName=${encodeURIComponent(instanceName)}&dbName=${encodeURIComponent(dbName)}`
            );
            return result.data || [];
        } catch (error) {
            console.error('Query tables error:', error);
            return [];
        }
    },

    async showTableStructure(dbName, tableName) {
        try {
            const result = await http.post(`${config.baseUrl}/table/structure`, {
                instanceName: config.instanceName,
                dbName: dbName,
                tableName: tableName
            });
            return result.data?.rows?.[0]?.[1] || '';
        } catch (error) {
            console.error('Show table structure error:', error);
            return '';
        }
    },

    async querySql(dbName, tableName, sql) {
        try {
            const result = await http.post(`${config.baseUrl}/query`, {
                instanceName: config.instanceName,
                dbName: dbName,
                tableName: tableName,
                sqlContent: sql,
                limitNum: config.pageSize.toString()
            });
            return result;
        } catch (error) {
            console.error('Query SQL error:', error);
            return { status: -1, msg: error.message };
        }
    },

    async countTable(dbName, tableName) {
        try {
            const sql = `select count(1) from ${tableName}`;
            const result = await this.querySql(dbName, tableName, sql);
            if (result.status === 0) {
                return result.data?.rows?.[0]?.[0] || 0;
            }
            return 0;
        } catch (error) {
            console.error('Count table error:', error);
            return 0;
        }
    }
};

// UI Controller
class UIController {
    constructor() {
        this.currentTabs = [];
        this.activeTabId = null;
        this.init();
    }

    init() {
        this.bindEvents();
        this.checkLogin();
    }

    bindEvents() {
        // Login button
        document.getElementById('loginBtn').addEventListener('click', () => this.handleLogin());
        
        // Enter key for login
        document.getElementById('password').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.handleLogin();
        });

        // Server selection
        document.getElementById('serverSelect').addEventListener('change', (e) => {
            this.handleServerChange(e.target.value);
        });

        // Page size selection
        document.getElementById('pageSizeSelect').addEventListener('change', async (e) => {
            config.pageSize = parseInt(e.target.value);
            await storage.set('pageSize', e.target.value);
        });

        // User button
        document.getElementById('userBtn').addEventListener('click', () => {
            this.showLoginModal();
        });
    }

    async checkLogin() {
        const username = await storage.get('username');
        if (username) {
            config.username = username;
            
            const servers = await api.queryServers();
            if (servers && servers.length > 0) {
                this.showMainApp();
                this.populateServers(servers);
                return;
            }
        }
        this.showLoginModal();
    }

    showLoginModal() {
        document.getElementById('loginModal').style.display = 'flex';
        document.getElementById('mainApp').style.display = 'none';
    }

    showMainApp() {
        document.getElementById('loginModal').style.display = 'none';
        document.getElementById('mainApp').style.display = 'block';
        document.getElementById('userBtn').textContent = `用户：${config.username}`;
    }

    async handleLogin() {
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        const errorDiv = document.getElementById('loginError');
        const loginBtn = document.getElementById('loginBtn');

        if (!username || !password) {
            errorDiv.textContent = '请输入账号和密码';
            errorDiv.classList.add('show');
            return;
        }

        loginBtn.disabled = true;
        loginBtn.textContent = '登录中...';
        errorDiv.classList.remove('show');

        const success = await api.login(username, password);

        if (success) {
            const servers = await api.queryServers();
            if (servers && servers.length > 0) {
                this.showMainApp();
                this.populateServers(servers);
            } else {
                errorDiv.textContent = '登录失败，请检查账号密码';
                errorDiv.classList.add('show');
            }
        } else {
            errorDiv.textContent = '登录失败，请检查账号密码';
            errorDiv.classList.add('show');
        }

        loginBtn.disabled = false;
        loginBtn.textContent = '登录';
    }

    populateServers(servers) {
        const select = document.getElementById('serverSelect');
        select.innerHTML = '<option value="">选择服务器...</option>';
        servers.forEach(server => {
            const option = document.createElement('option');
            option.value = server;
            option.textContent = server;
            select.appendChild(option);
        });

        storage.get('instance_name').then(savedInstance => {
            if (savedInstance && servers.includes(savedInstance)) {
                select.value = savedInstance;
                this.handleServerChange(savedInstance);
            }
        });
    }

    async handleServerChange(instanceName) {
        if (!instanceName) return;

        config.instanceName = instanceName;
        await storage.set('instance_name', instanceName);

        const treeView = document.getElementById('treeView');
        treeView.innerHTML = '<div class="tree-placeholder">加载中...</div>';

        const databases = await api.queryDatabases(instanceName);
        this.renderTreeView(databases);
    }

    renderTreeView(databases) {
        const treeView = document.getElementById('treeView');
        treeView.innerHTML = '';

        databases.forEach(dbName => {
            const nodeDiv = document.createElement('div');
            nodeDiv.className = 'tree-node';
            
            const labelDiv = document.createElement('div');
            labelDiv.className = 'tree-node-label';
            labelDiv.innerHTML = `<span class="tree-icon">▶</span><span>📁 ${dbName}</span>`;
            
            const childrenDiv = document.createElement('div');
            childrenDiv.className = 'tree-children';

            labelDiv.addEventListener('click', async () => {
                const icon = labelDiv.querySelector('.tree-icon');
                const isExpanded = childrenDiv.classList.contains('show');

                if (isExpanded) {
                    childrenDiv.classList.remove('show');
                    icon.classList.remove('expanded');
                } else {
                    if (childrenDiv.children.length === 0) {
                        childrenDiv.innerHTML = '<div class="tree-placeholder">加载中...</div>';
                        const tables = await api.queryTables(config.instanceName, dbName);
                        childrenDiv.innerHTML = '';
                        tables.forEach(tableName => {
                            const tableDiv = document.createElement('div');
                            tableDiv.className = 'tree-table';
                            tableDiv.innerHTML = `📊 ${tableName}`;
                            tableDiv.addEventListener('dblclick', () => {
                                this.openTableTab(dbName, tableName);
                            });
                            childrenDiv.appendChild(tableDiv);
                        });
                    }
                    childrenDiv.classList.add('show');
                    icon.classList.add('expanded');
                }
            });

            nodeDiv.appendChild(labelDiv);
            nodeDiv.appendChild(childrenDiv);
            treeView.appendChild(nodeDiv);
        });
    }

    openTableTab(dbName, tableName) {
        const tabId = `${dbName}.${tableName}`;
        
        // Check if tab already exists
        const existingTab = this.currentTabs.find(t => t.id === tabId);
        if (existingTab) {
            this.activateTab(tabId);
            return;
        }

        // Create new tab
        const tab = {
            id: tabId,
            dbName,
            tableName,
            sortColumn: '',
            sortAsc: true,  // Note: true means DESC order (matching C# version behavior)
            condition: ''
        };

        this.currentTabs.push(tab);
        this.renderTabs();
        this.activateTab(tabId);
        this.loadTableData(tab);
    }

    renderTabs() {
        const tabsHeader = document.getElementById('tabsHeader');
        const tabsContent = document.getElementById('tabsContent');

        tabsHeader.innerHTML = '';
        tabsContent.innerHTML = '';

        this.currentTabs.forEach(tab => {
            // Create tab header
            const tabButton = document.createElement('button');
            tabButton.className = 'tab';
            tabButton.innerHTML = `
                <span>${tab.tableName}</span>
                <span class="tab-close">×</span>
            `;
            
            tabButton.addEventListener('click', (e) => {
                if (e.target.classList.contains('tab-close')) {
                    this.closeTab(tab.id);
                } else {
                    this.activateTab(tab.id);
                }
            });

            tabsHeader.appendChild(tabButton);

            // Create tab content
            const tabPane = document.createElement('div');
            tabPane.className = 'tab-pane';
            tabPane.id = `tab-${tab.id}`;
            tabPane.innerHTML = `
                <div class="query-panel">
                    <div class="query-controls">
                        <button class="btn btn-sm btn-secondary" data-action="toggle-structure">表结构</button>
                        <input type="text" class="query-input" placeholder="输入WHERE条件，例如：id > 100" data-field="condition">
                        <button class="btn btn-sm btn-success" data-action="query">查询</button>
                        <button class="btn btn-sm btn-secondary" data-action="clear-sort">清空排序</button>
                        <button class="btn btn-sm btn-secondary" data-action="copy-sql">复制SQL</button>
                    </div>
                    <div class="query-info">
                        <span>服务：${config.instanceName}</span>
                        <span data-field="query-time">查询时间：-</span>
                        <span data-field="row-count">条数：-</span>
                        <button class="btn btn-sm btn-secondary" data-action="count-total">计算总数</button>
                        <span data-field="total-count">总数：-</span>
                    </div>
                    <div class="query-sql" data-field="sql">-</div>
                </div>
                <div class="table-structure" data-field="structure">
                    <pre></pre>
                </div>
                <div class="table-container">
                    <table class="data-table">
                        <thead data-field="table-head"></thead>
                        <tbody data-field="table-body"></tbody>
                    </table>
                </div>
            `;

            this.bindTabEvents(tabPane, tab);
            tabsContent.appendChild(tabPane);
        });
    }

    bindTabEvents(tabPane, tab) {
        // Toggle structure
        tabPane.querySelector('[data-action="toggle-structure"]').addEventListener('click', () => {
            const structureDiv = tabPane.querySelector('[data-field="structure"]');
            structureDiv.classList.toggle('show');
        });

        // Query button
        tabPane.querySelector('[data-action="query"]').addEventListener('click', () => {
            tab.condition = tabPane.querySelector('[data-field="condition"]').value.trim();
            this.loadTableData(tab);
        });

        // Clear sort
        tabPane.querySelector('[data-action="clear-sort"]').addEventListener('click', () => {
            tab.sortColumn = '';
            tab.sortAsc = true;
            this.loadTableData(tab);
        });

        // Copy SQL
        tabPane.querySelector('[data-action="copy-sql"]').addEventListener('click', () => {
            const sql = tabPane.querySelector('[data-field="sql"]').textContent;
            navigator.clipboard.writeText(sql);
        });

        // Count total
        tabPane.querySelector('[data-action="count-total"]').addEventListener('click', async () => {
            const btn = tabPane.querySelector('[data-action="count-total"]');
            btn.disabled = true;
            btn.textContent = '计算中...';
            
            const count = await api.countTable(tab.dbName, tab.tableName);
            tabPane.querySelector('[data-field="total-count"]').textContent = `总数：${count}`;
            
            btn.disabled = false;
            btn.textContent = '计算总数';
        });
    }

    activateTab(tabId) {
        this.activeTabId = tabId;
        
        document.querySelectorAll('.tab').forEach((tab, index) => {
            if (this.currentTabs[index].id === tabId) {
                tab.classList.add('active');
            } else {
                tab.classList.remove('active');
            }
        });

        document.querySelectorAll('.tab-pane').forEach(pane => {
            if (pane.id === `tab-${tabId}`) {
                pane.classList.add('active');
            } else {
                pane.classList.remove('active');
            }
        });
    }

    closeTab(tabId) {
        const index = this.currentTabs.findIndex(t => t.id === tabId);
        if (index > -1) {
            this.currentTabs.splice(index, 1);
            
            if (this.currentTabs.length === 0) {
                document.getElementById('tabsHeader').innerHTML = '';
                document.getElementById('tabsContent').innerHTML = `
                    <div class="welcome-screen">
                        <div class="welcome-icon">📊</div>
                        <h2>欢迎使用 MySQL 数据查看器</h2>
                        <p>请从左侧选择数据库表来查看数据</p>
                    </div>
                `;
            } else {
                this.renderTabs();
                const newActiveTab = this.currentTabs[Math.max(0, index - 1)];
                this.activateTab(newActiveTab.id);
            }
        }
    }

    async loadTableData(tab) {
        const tabPane = document.getElementById(`tab-${tab.id}`);
        if (!tabPane) return;

        // Build SQL
        // Note: sortAsc=true generates DESC order (matching C# version's confusing but intentional naming)
        let sql = `select * from ${tab.tableName}`;
        if (tab.condition) {
            sql += ` where ${tab.condition}`;
        }
        if (tab.sortColumn) {
            sql += ` order by ${tab.sortColumn}${tab.sortAsc ? ' DESC' : ''}`;
        }

        tabPane.querySelector('[data-field="sql"]').textContent = sql;

        // Load table structure if not loaded
        const structureDiv = tabPane.querySelector('[data-field="structure"] pre');
        if (!structureDiv.textContent) {
            const structure = await api.showTableStructure(tab.dbName, tab.tableName);
            structureDiv.textContent = structure;
        }

        // Query data
        const result = await api.querySql(tab.dbName, tab.tableName, sql);

        if (result.status !== 0) {
            alert(result.msg || '查询失败');
            return;
        }

        const data = result.data;
        tabPane.querySelector('[data-field="query-time"]').textContent = `查询时间：${data.query_time}`;
        tabPane.querySelector('[data-field="row-count"]').textContent = `条数：${data.affected_rows}`;

        this.renderTable(tabPane, tab, data);
    }

    renderTable(tabPane, tab, data) {
        const thead = tabPane.querySelector('[data-field="table-head"]');
        const tbody = tabPane.querySelector('[data-field="table-body"]');

        // Render header
        thead.innerHTML = '';
        const headerRow = document.createElement('tr');
        data.column_list.forEach(column => {
            const th = document.createElement('th');
            th.textContent = column;
            th.addEventListener('click', () => {
                if (tab.sortColumn === column) {
                    tab.sortAsc = !tab.sortAsc;
                } else {
                    tab.sortColumn = column;
                    tab.sortAsc = true;
                }
                this.loadTableData(tab);
            });

            if (tab.sortColumn === column) {
                th.classList.add(tab.sortAsc ? 'sorted-asc' : 'sorted-desc');
            }

            headerRow.appendChild(th);
        });
        thead.appendChild(headerRow);

        // Render body
        tbody.innerHTML = '';
        data.rows.forEach(row => {
            const tr = document.createElement('tr');
            row.forEach(cell => {
                const td = document.createElement('td');
                td.textContent = cell !== null ? cell : 'NULL';
                td.title = cell !== null ? cell : 'NULL';
                tr.appendChild(td);
            });
            tbody.appendChild(tr);
        });
    }
}

// Initialize app
document.addEventListener('DOMContentLoaded', async () => {
    // Load saved page size
    const savedPageSize = await storage.get('pageSize');
    if (savedPageSize) {
        config.pageSize = parseInt(savedPageSize);
        document.getElementById('pageSizeSelect').value = savedPageSize;
    }

    new UIController();
});
