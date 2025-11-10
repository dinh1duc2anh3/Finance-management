import { loadingManager } from './loadingUtils.js';

// Read configId from query string
const urlParams = new URLSearchParams(window.location.search);
const configId = urlParams.get('configId');

if (!configId) {
    console.error('Missing configId in URL. Redirecting to home.');
    alert('Missing configId. Please pick a sheet from the home page.');
    window.location.href = '/';
}

// Wire Add buttons to include configId when navigating to add page
function setupAddButtons() {
    const addBtn = document.getElementById('addBtn');
    const addBtnFooter = document.getElementById('addBtnFooter');
    const target = `/add?configId=${encodeURIComponent(configId)}`;
    if (addBtn) addBtn.addEventListener('click', () => window.location.href = target);
    if (addBtnFooter) addBtnFooter.addEventListener('click', () => window.location.href = target);
}

// -------------------------
// --- Element references ---
// -------------------------
const elements = {
    tableBody: document.getElementById('transactionsTableBody'),
    selectAllCheckbox: document.getElementById('selectAllCheckbox'),
    deleteSelectedBtn: document.getElementById('deleteSelectedBtn'),
    transactions: []
};

// -------------------------
// --- Data loading ---
// -------------------------
async function loadTransactions() {
    try {
        loadingManager.showLoading('Loading transactions...');
        const response = await fetch(`/read-sheet?configId=${encodeURIComponent(configId)}`);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        
        // First row is header, skip it
        elements.transactions = (data || []).slice(1).map((row, index) => ({
            rowIndex: index + 2, // +2 because: 1 for header, 1 for 0-based to 1-based
            date: row[0] || '',
            time: row[1] || '',
            transaction: row[2] || '',
            group: row[3] || '',
            subgroup: row[4] || '',
            category: row[5] || '',
            amount: row[6] || '',
            note: row[7] || ''
        }));
        
        renderTable();
    } catch (error) {
        console.error('Error loading transactions:', error);
        elements.tableBody.innerHTML = `
            <tr>
                <td colspan="11" class="text-center text-danger">
                    Error loading transactions: ${error.message}
                </td>
            </tr>
        `;
    } finally {
        loadingManager.hideLoading();
    }
}

// -------------------------
// --- Table rendering ---
// -------------------------
function renderTable() {
    if (elements.transactions.length === 0) {
        elements.tableBody.innerHTML = `
            <tr>
                <td colspan="11" class="text-center text-muted">
                    No transactions found
                </td>
            </tr>
        `;
        return;
    }

    elements.tableBody.innerHTML = elements.transactions.map((transaction, index) => `
        <tr data-row-index="${transaction.rowIndex}">
            <td class="checkbox-column">
                <input 
                    type="checkbox" 
                    class="row-checkbox" 
                    data-row-index="${transaction.rowIndex}"
                />
            </td>
            <td class="order-column">${index + 1}</td>
            <td>${formatDate(transaction.date)}</td>
            <td>${formatTime(transaction.time)}</td>
            <td>${escapeHtml(transaction.transaction)}</td>
            <td>${escapeHtml(transaction.group)}</td>
            <td>${escapeHtml(transaction.subgroup)}</td>
            <td>${escapeHtml(transaction.category)}</td>
            <td>${formatAmount(transaction.amount)}</td>
            <td>${escapeHtml(transaction.note)}</td>
            <td class="action-column">
                <div class="dropdown action-menu">
                    <button 
                        class="action-btn dropdown-toggle" 
                        type="button" 
                        data-bs-toggle="dropdown"
                        aria-expanded="false"
                    >
                        <img src="/images/hamburger.svg" alt="Menu" style="width: 14px; height: 14px; margin-right: 6px; vertical-align: middle;" />
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end">
                        <li>
                            <a class="dropdown-item clone-action" href="#" data-row-index="${transaction.rowIndex}">
                                <img src="/images/clone.svg" alt="Clone" style="width: 14px; height: 14px; margin-right: 6px; vertical-align: middle;" />
                                Clone
                            </a>
                        </li>
                        <li>
                            <a class="dropdown-item text-danger delete-action" href="#" data-row-index="${transaction.rowIndex}">
                                <img src="/images/delete.svg" alt="Delete" style="width: 14px; height: 14px; margin-right: 6px; vertical-align: middle;" />
                                Delete
                            </a>
                        </li>
                    </ul>
                </div>
            </td>
        </tr>
    `).join('');

    // Attach checkbox event listeners
    document.querySelectorAll('.row-checkbox').forEach(checkbox => {
        checkbox.addEventListener('change', updateDeleteButtonState);
    });
}

// -------------------------
// --- Selection management ---
// -------------------------
function updateDeleteButtonState() {
    const checkedBoxes = document.querySelectorAll('.row-checkbox:checked');
    elements.deleteSelectedBtn.disabled = checkedBoxes.length === 0;
}

elements.selectAllCheckbox.addEventListener('change', (e) => {
    const checkboxes = document.querySelectorAll('.row-checkbox');
    checkboxes.forEach(checkbox => {
        checkbox.checked = e.target.checked;
    });
    updateDeleteButtonState();
});

// -------------------------
// --- Delete operations ---
// -------------------------
async function deleteSingleTransaction(rowIndex) {
    if (!confirm('Are you sure you want to delete this transaction?')) {
        return;
    }

    try {
        loadingManager.showLoading('Deleting transaction...');
        const response = await fetch(`/delete-row/${rowIndex}?configId=${encodeURIComponent(configId)}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `HTTP error! status: ${response.status}`);
        }

        const message = await response.text();
        console.log('Delete successful:', message);
        alert('Transaction deleted successfully!');
        await loadTransactions();
    } catch (error) {
        console.error('Error deleting transaction:', error);
        alert(`Failed to delete transaction: ${error.message}`);
    } finally {
        loadingManager.hideLoading();
    }
}

async function deleteSelectedTransactions() {
    const checkedBoxes = document.querySelectorAll('.row-checkbox:checked');
    if (checkedBoxes.length === 0) {
        alert('Please select at least one transaction to delete.');
        return;
    }

    if (!confirm(`Are you sure you want to delete ${checkedBoxes.length} selected transaction(s)?`)) {
        return;
    }

    const rowIndices = Array.from(checkedBoxes).map(cb => 
        parseInt(cb.getAttribute('data-row-index'))
    ).sort((a, b) => b - a); // Sort descending for safe deletion

    try {
        loadingManager.showLoading('Deleting transactions...');
        const response = await fetch(`/delete-rows?configId=${encodeURIComponent(configId)}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(rowIndices)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `HTTP error! status: ${response.status}`);
        }

        const message = await response.text();
        console.log('Delete successful:', message);
        alert(`${rowIndices.length} transaction(s) deleted successfully!`);
        await loadTransactions();
        elements.selectAllCheckbox.checked = false;
    } catch (error) {
        console.error('Error deleting transactions:', error);
        alert(`Failed to delete transactions: ${error.message}`);
    } finally {
        loadingManager.hideLoading();
    }
}

elements.deleteSelectedBtn.addEventListener('click', deleteSelectedTransactions);

// -------------------------
// --- Clone operation ---
// -------------------------
async function cloneTransaction(rowIndex) {
    const transaction = elements.transactions.find(t => t.rowIndex === rowIndex);
    if (!transaction) {
        alert('Transaction not found!');
        return;
    }

    try {
        loadingManager.showLoading('Cloning transaction...');
        const response = await fetch(`/clone-row?configId=${encodeURIComponent(configId)}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                rowIndex: rowIndex
            })
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `HTTP error! status: ${response.status}`);
        }

        const message = await response.text();
        console.log('Clone successful:', message);
        alert('Transaction cloned successfully!');
        await loadTransactions();
    } catch (error) {
        console.error('Error cloning transaction:', error);
        alert(`Failed to clone transaction: ${error.message}`);
    } finally {
        loadingManager.hideLoading();
    }
}

// Functions are used via event delegation, no need to expose globally

// -------------------------
// --- Utility functions ---
// -------------------------
function formatDate(dateStr) {
    if (!dateStr) return '';
    // If date is in format YYYY-MM-DD, convert to DD/MM/YYYY
    if (dateStr.match(/^\d{4}-\d{2}-\d{2}$/)) {
        const [year, month, day] = dateStr.split('-');
        return `${day}/${month}/${year}`;
    }
    return dateStr;
}

function formatTime(timeStr) {
    if (!timeStr) return '';
    // If time is in format HH:MM, ensure it's displayed as HH:MM:SS
    if (timeStr.match(/^\d{2}:\d{2}$/)) {
        return timeStr + ':00';
    }
    return timeStr;
}

function formatAmount(amountStr) {
    if (!amountStr) return '';
    // Remove any existing formatting and add thousand separators
    const numStr = amountStr.toString().replace(/\D/g, '');
    if (!numStr) return '';
    return Number(numStr).toLocaleString('vi-VN');
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// -------------------------
// --- Event delegation setup ---
// -------------------------
function setupEventDelegation() {
    // Attach dropdown menu action listeners using event delegation
    elements.tableBody.addEventListener('click', (e) => {
        if (e.target.classList.contains('clone-action') || e.target.closest('.clone-action')) {
            e.preventDefault();
            const action = e.target.classList.contains('clone-action') ? e.target : e.target.closest('.clone-action');
            const rowIndex = parseInt(action.getAttribute('data-row-index'));
            cloneTransaction(rowIndex);
            // Close dropdown
            const dropdown = action.closest('.dropdown');
            const bsDropdown = bootstrap.Dropdown.getInstance(dropdown.querySelector('.dropdown-toggle'));
            if (bsDropdown) {
                bsDropdown.hide();
            }
        } else if (e.target.classList.contains('delete-action') || e.target.closest('.delete-action')) {
            e.preventDefault();
            const action = e.target.classList.contains('delete-action') ? e.target : e.target.closest('.delete-action');
            const rowIndex = parseInt(action.getAttribute('data-row-index'));
            deleteSingleTransaction(rowIndex);
            // Close dropdown
            const dropdown = action.closest('.dropdown');
            const bsDropdown = bootstrap.Dropdown.getInstance(dropdown.querySelector('.dropdown-toggle'));
            if (bsDropdown) {
                bsDropdown.hide();
            }
        }
    });
}

// -------------------------
// --- Initialize ---
// -------------------------
setupAddButtons();
setupEventDelegation();
loadTransactions();
