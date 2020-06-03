(function () {
    if (window.accountUtils !== undefined) {
        return;
    }
    const accountUtils = {};

    accountUtils.store = {
        orders: [],
        account: {},
        accountViews: [],
        orderViews: [],
        editAccount: null,
    };

    accountUtils.templates = {};


    accountUtils.init = function () {
        accountUtils.templates.accountRecord = document.querySelector('#account-template-record').content;
        accountUtils.templates.orderRecord = document.querySelector('#order-template-record').content;

        const createAccountRecordView = document.body.querySelector('.edit-account-wrapper');
        accountUtils.store.createAccountRecordView = createAccountRecordView;
        createAccountRecordView.style.display = 'none';

        document.body.querySelector('.orders-table').style.display = 'none';

        createAccountRecordView.querySelector('.edit-account-cancel-btn')
            .addEventListener('click', () => {
                accountUtils.spinner.fullScreenOverlay.style.display = 'none';
                accountUtils.store.createAccountRecordView.style.display = 'none';
            });

        createAccountRecordView.querySelector('.edit-account-save-btn')
            .addEventListener('click', function () {
                accountUtils.spinner.fullScreenOverlay.style.display = 'none';
                accountUtils.store.createAccountRecordView.style.display = 'none';
                const id = document.body.querySelector('.edit-account-id').value;
                const amount = document.body.querySelector('.edit-account-amount').value;

                let account = {
                    id: id,
                    amount: amount,
                    user: {
                        id
                    }
                };
                const editAccount = accountUtils.store.editAccount;
                account.id = editAccount.id;
                account.user.id = editAccount.user.id;
                accountUtils.ajax
                    .updateAccount(account)
                    .then(
                        (data) => {
                            accountUtils.account.refreshTables();
                        })
                    .catch((err) => {
                        console.error(err);
                        showModal(err)
                    });
            });

        document.body.querySelector('.orders-info')
            .addEventListener('click', () => {
                accountUtils.order.cleanTable();
                accountUtils.order.fillTable();
                document.body.querySelector('.orders-table').style.display = '';
            });
        document.body.querySelector('.create-order')
            .addEventListener('click', () => {
                accountUtils.spinner.showSpinner();
                const accountId = accountUtils.store.account.id;
                let order = {
                    id: null,
                    status: null,
                    totalSum: null,
                    account: {
                        id: accountId
                    }
                };
                accountUtils.ajax
                    .createOrder(order)
                    .then(
                        (data) => {
                            accountUtils.account.refreshTables();
                            accountUtils.ajax.getOrdersByAccountId(accountId)
                                .then((response) => {
                                    const size = accountUtils.store.orders.length;
                                    order = accountUtils.store.orders[size - 1];
                                    accountUtils.ajax.getOrderInfo(order.id);
                                })
                                .catch((err) => {
                                    console.error(err);
                                    showModal(err)
                                });
                        })
                    .catch((err) => {
                        console.error(err);
                        showModal(err)
                    });
            });
        document.body.querySelector('.refresh-table')
            .addEventListener('click', () => {
                accountUtils.account.refreshTables();
            });
        document.body.querySelector('.back')
            .addEventListener('click', () => {
                location.href = '/';
            });

        accountUtils.store.accountTable = document.querySelector('.account-table-content');
        accountUtils.store.ordersTable = document.querySelector('.orders-table-content');
        accountUtils.store.modal = document.getElementById("myModal");
        accountUtils.store.span = document.getElementsByClassName("close")[0];
    };


    accountUtils.spinner = {};
    accountUtils.spinner.spinnerContainerClassName = 'users-spinner-container';
    accountUtils.spinner.spinnerClassName = 'users-spinner';

    accountUtils.spinner.createSpinner = function () {
        if (document.body.querySelector(`.${this.spinnerContainerClassName}`)) {
            return;
        }

        this.fullScreenOverlay = document.querySelector('.full-screen-overlay');
        this.fullScreenOverlay.style.display = 'none';
        const spinnerContainer = document.createElement('div');
        spinnerContainer.classList.add(this.spinnerContainerClassName);

        const spinner = document.createElement('div');
        spinner.classList.add(this.spinnerClassName);
        spinnerContainer.append(spinner);

        spinner.innerHTML = ` <div class="sk-grid">
                                   <div class="sk-grid-cube"></div>
                                   <div class="sk-grid-cube"></div>
                                   <div class="sk-grid-cube"></div>
                                   <div class="sk-grid-cube"></div>
                                   <div class="sk-grid-cube"></div>
                                   <div class="sk-grid-cube"></div>
                                   <div class="sk-grid-cube"></div>
                                   <div class="sk-grid-cube"></div>
                                   <div class="sk-grid-cube"></div>
                               </div>
                             `;
        document.body.prepend(spinnerContainer);
        accountUtils.spinner.spinnerContainer = spinnerContainer;
    };

    accountUtils.spinner.showSpinner = function () {
        this.fullScreenOverlay.style.display = '';
        this.spinnerContainer.style.display = '';
    };
    accountUtils.spinner.hideSpinner = function () {
        this.fullScreenOverlay.style.display = 'none';
        this.spinnerContainer.style.display = 'none';
    };


    accountUtils.ajax = {
        baseUrl: '',
        get accountUrl() {
            return this.baseUrl + '/accounts';
        },
        get ordersUrl() {
            return this.baseUrl + '/orders';
        }
    };

    accountUtils.ajax.getOrdersByAccountId = function (accountId) {
        accountUtils.spinner.showSpinner();
        return window.axios
            .get(`${this.ordersUrl}/account/${accountId}`)
            .then((response) => {
                accountUtils.store.orders = response.data;
                accountUtils.spinner.hideSpinner();
            })
            .catch((err) => {
                console.error(err);
                showModal(err);
                accountUtils.spinner.hideSpinner();
            });
    };

    accountUtils.ajax.findAccountById = function () {
        accountUtils.spinner.showSpinner();
        let accountId = window.location.pathname.split('/')[2];
        return window.axios
            .get(`${this.accountUrl}/${accountId}`)
            .then((response) => {
                accountUtils.store.account = response.data;
                accountUtils.spinner.hideSpinner();
            })
            .catch((err) => {
                console.error(err);
                accountUtils.store.account = null;
                showModal(err);
                accountUtils.spinner.hideSpinner();
            });
    };

    accountUtils.ajax.backLink = function () {
        location.href = '/';
    };

    accountUtils.ajax.deleteAccountById = function (id) {
        return window.axios.delete(`${this.accountUrl}/${id}`)
            .then((response) => {
                accountUtils.store.account = null;
                accountUtils.account.refreshTables();
            })
    };

    accountUtils.ajax.deleteOrderById = function (id) {
        return window.axios.delete(`${this.ordersUrl}/${id}`);
    };

    accountUtils.ajax.saveAccount = function (account) {
        return window.axios.post(`${this.accountUrl}`, account);
    };

    accountUtils.ajax.updateAccount = function (account) {
        accountUtils.spinner.showSpinner();
        return window.axios.put(`${this.accountUrl}/${account.id}`, account)
            .catch((err) => {
                console.error(err);
                showModal(err);
                accountUtils.spinner.hideSpinner();
            });
    };

    accountUtils.ajax.createOrder = function (order) {
        return window.axios.post(`${this.ordersUrl}`, order);
    }

    accountUtils.account = {};
    accountUtils.order = {};
    accountUtils.account.refreshTables = function () {
        accountUtils.ajax.findAccountById().then((data) => {
            accountUtils.account.cleanTable();
            accountUtils.order.cleanTable();
            if (accountUtils.store.account) {
                accountUtils.ajax.getOrdersByAccountId(accountUtils.store.account.id)
                    .then((response) => {
                        accountUtils.account.fillTable();
                    })
            }
        })
    };

    accountUtils.account.edit = function (account) {
        accountUtils.store.createAccountRecordView.style.display = '';
        accountUtils.spinner.fullScreenOverlay.style.display = '';
        const id = document.body.querySelector('.edit-account-id');
        const amount = document.body.querySelector('.edit-account-amount');
        accountUtils.store.editAccount = account;
        id.value = account.id;
        amount.value = account.amount;
    };

    accountUtils.ajax.ordersInfo = function (accountId) {
        return window.axios.get(`${this.ordersUrl}/account/${accountId}`);
    };

    accountUtils.ajax.getOrderInfo = function (orderId) {
        location.href = `${this.ordersUrl}/${orderId}/info`;
    };

    accountUtils.account.delete = function (account) {
        accountUtils.spinner.showSpinner();
        accountUtils.ajax.deleteAccountById(account.id)
            .then((response) => {
                accountUtils.ajax.backLink();
                accountUtils.spinner.hideSpinner();
            })
            .catch((err) => {
                console.error(err);
                showModal(err);
                accountUtils.spinner.hideSpinner();
            });
    };
    accountUtils.order.delete = function (account) {
        accountUtils.spinner.showSpinner();
        accountUtils.ajax.deleteOrderById(account.id)
            .then((response) => {
                accountUtils.account.refreshTables();
                accountUtils.spinner.hideSpinner();
            })
            .catch((err) => {
                console.error(err);
                showModal(err);
                accountUtils.spinner.hideSpinner();
            });
    };

    function showModal(err) {
        let modalMessage = accountUtils.store.modal.querySelector('.text');
        let errorData = err.response.data;
        if (errorData.hasOwnProperty("message")) {
            modalMessage.innerHTML = errorData.message;
        } else {
            modalMessage.innerHTML = JSON.stringify(errorData);
        }
        accountUtils.store.modal.style.display = "block";
        accountUtils.store.span.onclick = function () {
            accountUtils.store.modal.style.display = "none";
        }
    };

    accountUtils.order.delete = function (order) {
        accountUtils.spinner.showSpinner();
        accountUtils.ajax.deleteOrderById(order.id)
            .then((response) => {
                accountUtils.spinner.hideSpinner();
                accountUtils.account.refreshTables();
            })
            .catch((err) => {
                console.error(err);
                showModal(err);
                accountUtils.spinner.hideSpinner();
            });
    };
    accountUtils.order.createRecord = function (order) {
        const viewTemplate = accountUtils.templates.orderRecord;
        viewTemplate.querySelector('.order-id').innerText = order.id;
        viewTemplate.querySelector('.order-status').innerText = order.status;
        viewTemplate.querySelector('.order-total-sum').innerText = order.totalSum;
        let view = document.importNode(viewTemplate, true);
        view.orderId = order.id;
        view.querySelector('.order-info-btn')
            .addEventListener('click', function (event) {
                accountUtils.ajax.getOrderInfo(order.id);
            });
        view.querySelector('.order-delete-btn')
            .addEventListener('click', function () {
                accountUtils.order.delete(order);
            });
        accountUtils.store.orderViews.push(view);
        return view;
    };

    accountUtils.account.createRecord = function (account) {
        const viewTemplate = accountUtils.templates.accountRecord;
        viewTemplate.querySelector('.account-id').innerText = account.id;
        viewTemplate.querySelector('.account-amount').innerText = account.amount;
        let view = document.importNode(viewTemplate, true);
        view.accountId = account.id;

        view.querySelector('.account-edit-btn')
            .addEventListener('click', function (event) {
                accountUtils.account.edit(account)
            });
        view.querySelector('.account-delete-btn')
            .addEventListener('click', function () {
                accountUtils.account.delete(account);
            });

        accountUtils.store.accountViews.push(view);

        return view;
    };

    accountUtils.order.fillTable = function () {
        accountUtils.store.orders.forEach((o) => {
            accountUtils.store.ordersTable.append(accountUtils.order.createRecord(o));
        })
    };
    accountUtils.account.fillTable = function () {
        accountUtils.store.accountTable.append(accountUtils.account.createRecord(accountUtils.store.account));
    };

    accountUtils.account.cleanTable = function () {
        accountUtils.store.accountTable.innerHTML = '';
    };
    accountUtils.order.cleanTable = function () {
        accountUtils.store.ordersTable.innerHTML = '';
    };

    window.accountUtils = accountUtils;
}());