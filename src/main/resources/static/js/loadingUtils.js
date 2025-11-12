/**
 * Utility class for managing loading overlay state
 * Can be reused across different pages/components
 */

class LoadingManager {
    constructor() {
        this.overlayId = 'loadingOverlay';
    }

    /**
     * Show loading overlay with optional custom message
     * @param {string} message - Optional custom message (default: "Loading...")
     */
    showLoading(message = "Loading...") {
        this.hideLoading();

        const overlay = document.createElement('div');
        overlay.id = this.overlayId;
        overlay.innerHTML = `
            <div style="
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0,0,0,0.5);
                display: flex;
                justify-content: center;
                align-items: center;
                z-index: 9999;
            ">
                <div style="
                    background: white;
                    padding: 30px;
                    border-radius: 12px;
                    text-align: center;
                    box-shadow: 0 4px 20px rgba(0,0,0,0.3);
                ">
                    <div class="spinner-border text-primary" role="status" style="width: 3rem; height: 3rem;">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                    <p class="mt-3 mb-0" style="font-size: 16px; color: #333;">${message}</p>
                </div>
            </div>
        `;
        document.body.appendChild(overlay);
    }

    hideLoading() {
        const overlay = document.getElementById(this.overlayId);
        if (overlay) {
            overlay.remove();
        }
    }
}

// Export singleton instance for easy use
export const loadingManager = new LoadingManager();