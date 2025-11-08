import { categoryData } from './config.js';
import { loadingManager } from './loadingUtils.js';
// -------------------------
// --- Element references ---
// -------------------------
const elements = {
    dateInput: document.getElementById("date"),
    timeInput: document.getElementById("time"),
    transactionInput: document.getElementById("transaction"),
    groupSelect: document.getElementById("group"),
    subgroupSelect: document.getElementById("subgroup"),
    categoryInput: document.getElementById("category"),
    resetGroupBtn: document.getElementById("resetGroupBtn"),
    amountInput: document.getElementById("amount"),
    noteInput: document.getElementById("note"),
    form: document.getElementById("transactionForm")
};

// -------------------------
// --- Data structures ---
// -------------------------
const categoryMap = {}; // category → {group, subgroup}
let isSubmitting = false; // Flag để ngăn submit nhiều lần

// -------------------------
// --- Setup functions ---
// -------------------------
function setupCategoryMap() {
    for (const group in categoryData) {
        for (const subgroup in categoryData[group]) {
            categoryData[group][subgroup].forEach(cat => {
                categoryMap[cat] = { group, subgroup };
            });
        }
    }
}

function setupDateTime() {
    const now = new Date();
    elements.dateInput.value = now.toISOString().slice(0,10);
    elements.timeInput.value = now.toTimeString().slice(0,5);
}

function setupAwesomplete() {
    elements.awesompleteInstance = new Awesomplete(elements.categoryInput, {
        minChars: 0,
        maxItems: 50,
        autoFirst: true
    });
}

function getCategoryList() {
    const group = elements.groupSelect.value;
    const subgroup = elements.subgroupSelect.value;

    if (group && subgroup) {
        return categoryData[group][subgroup] || [];
    } else if (group && !subgroup) {
        return Object.values(categoryData[group]).flat();
    } else {
        return Object.values(categoryData).flatMap(sg => Object.values(sg).flat());
    }
}

function updateCategorySuggestions() {
    elements.awesompleteInstance.list = getCategoryList();
}

function populateSubgroupOptions(group) {
    elements.subgroupSelect.innerHTML = '<option value="">-- Select Subgroup --</option>';
    if (group && categoryData[group]) {
        Object.keys(categoryData[group]).forEach(sg => {
            const opt = document.createElement("option");
            opt.value = sg;
            opt.textContent = sg;
            elements.subgroupSelect.appendChild(opt);
        });
    }
}

function handleCategorySelectComplete() {
    const categoryInput = elements.categoryInput.value.trim();
    const result = categoryMap[categoryInput];
    if (!result) return;

    elements.groupSelect.value = result.group;
    populateSubgroupOptions(result.group);
    elements.subgroupSelect.value = result.subgroup;
    updateCategorySuggestions();
    console.log("lookup result:", result);
}

function setupCategoryInputEvents() {
    elements.categoryInput.addEventListener("input", updateCategorySuggestions);
    elements.categoryInput.addEventListener("focus", updateCategorySuggestions);
    elements.categoryInput.addEventListener("awesomplete-selectcomplete", handleCategorySelectComplete);
}

function setupGroupSubgroupEvents() {
    elements.groupSelect.addEventListener("change", () => {
        populateSubgroupOptions(elements.groupSelect.value);
        elements.categoryInput.value = '';
        updateCategorySuggestions();
    });

    elements.subgroupSelect.addEventListener("change", () => {
        elements.categoryInput.value = '';
        updateCategorySuggestions();
    });
}

function resetGroup() {
    // Reset group, subgroup, and category
    elements.groupSelect.value = '';
    elements.subgroupSelect.innerHTML = '<option value="">-- Select Subgroup --</option>';
    elements.categoryInput.value = '';
    updateCategorySuggestions();
}


function setupResetGroupButton() {
    if (elements.resetGroupBtn) {
        elements.resetGroupBtn.addEventListener("click", (e) => {
            e.preventDefault();
            resetGroup();
        });
    }
}

function setupAmountInput() {
    elements.amountInput.addEventListener("input", () => {
        let value = elements.amountInput.value.replace(/\D/g, "");
        elements.amountInput.value = value ? Number(value).toLocaleString("vi-VN") : "";
    });
}

function setupNoteInput() {
    elements.noteInput.addEventListener("keydown", e => {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            elements.form.requestSubmit();
        }
    });
}

function disableForm() {
    elements.form.querySelectorAll('input, select, textarea, button').forEach(el => {
        el.disabled = true;
    });
    // Thêm class để làm mờ form
    elements.form.style.opacity = '0.6';
    elements.form.style.pointerEvents = 'none';
}

function enableForm() {
    elements.form.querySelectorAll('input, select, textarea, button').forEach(el => {
        el.disabled = false;
    });
    // Khôi phục form về bình thường
    elements.form.style.opacity = '1';
    elements.form.style.pointerEvents = 'auto';
}

function setupFormSubmit() {
    elements.form.addEventListener("submit", async (e) => {
        e.preventDefault();
        
        // Ngăn submit nhiều lần
        if (isSubmitting) {
            console.log("Request already in progress, ignoring...");
            return;
        }
        
        isSubmitting = true;
        loadingManager.showLoading(); // Hiển thị loading
        disableForm(); // Disable form
        
        try {
            const formData = {
                date: elements.dateInput ? elements.dateInput.value : "",
                time: elements.timeInput ? elements.timeInput.value : "",
                transaction: elements.transactionInput ? elements.transactionInput.value : "",
                group: elements.groupSelect ? elements.groupSelect.value : "",
                subgroup: elements.subgroupSelect ? elements.subgroupSelect.value : "",
                category: elements.categoryInput ? elements.categoryInput.value : "",
                amount: elements.amountInput ? elements.amountInput.value : "",
                note: elements.noteInput ? elements.noteInput.value : ""
            }

            const response = await fetch('/append', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(formData)
            });
            
            const msg = await response.text();

            if (response.ok) {
                console.log("Submitted:", msg);
                alert(msg || "Transaction saved!");
                resetForm();
            } else {
                // Server trả về lỗi (4xx, 5xx)
                const errorMsg = msg || `Server error: ${response.status} ${response.statusText}`;
                console.error('Server error:', errorMsg);
                alert(`Failed to save transaction!\n\n${errorMsg}`);
            }
        } catch (err) {
            // Network error hoặc lỗi khác
            console.error('Error:', err);
            const errorMsg = err.message || "Network error or server unavailable";
            alert(`Failed to save transaction!\n\n${errorMsg}\n\nPlease check your connection and try again.`);
        } finally {
            isSubmitting = false;
            loadingManager.hideLoading();
            enableForm();
        }
    });
}

function setupResetButton() {
    const resetBtn = elements.form.querySelector('button[type="reset"]');
    resetBtn.addEventListener("click", e => {
        e.preventDefault();        // ngăn reset mặc định
        console.log("Form reset.");
        resetForm();
    });
}

function resetForm() {
    elements.form.reset();
    setupDateTime();
}

// -------------------------
// --- Main setup function ---
// -------------------------
function setup() {
    setupCategoryMap();
    setupDateTime();
    setupAwesomplete();
    setupCategoryInputEvents();
    setupGroupSubgroupEvents();
    setupResetGroupButton();
    setupAmountInput();
    setupNoteInput();
    setupFormSubmit();
    setupResetButton();
}

setup();