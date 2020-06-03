document.addEventListener("DOMContentLoaded", function () {
    const utils = window.orderCardUtils;
    utils.init();
    utils.spinner.createSpinner();
    utils.spinner.hideSpinner();
    utils.orderCard.refreshTable();
});
