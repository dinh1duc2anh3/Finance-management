import { categoryData } from './config.js';

// -------------------------
// --- Element references ---
// -------------------------
const elements = {
    dateInput: document.getElementById("date"),
    timeInput: document.getElementById("time"),
    groupSelect: document.getElementById("group"),
    subgroupSelect: document.getElementById("subgroup"),
    categoryInput: document.getElementById("category"),
    amountInput: document.getElementById("amount"),
    noteInput: document.getElementById("note"),
    form: document.getElementById("transactionForm")
};

// -------------------------
// --- Data structures ---
// -------------------------
const categoryMap = {}; // category → {group, subgroup}

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

function setupFormSubmit() {
    elements.form.addEventListener("submit", e => {
        e.preventDefault();
        const formData = Object.fromEntries(new FormData(elements.form).entries());
        console.log("Submitted:", formData);
        alert("Transaction saved!");
        elements.form.reset();
        setupDateTime();
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
    setupAmountInput();
    setupNoteInput();
    setupFormSubmit();
    setupResetButton();
}

setup();