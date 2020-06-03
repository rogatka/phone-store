(function () {
    if (window.orderCardUtils !== undefined) {
        return;
    }

    const orderCardUtils = {};

    orderCardUtils.store = {
        phones: [],
        order: {},
        orderCards: [],
        orderCardViews: [],
        editOrderCard: null,
    };

    orderCardUtils.templates = {};


    orderCardUtils.init = function () {
        orderCardUtils.templates.record = document.querySelector('#template-record').content;

        const createRecordView = document.body.querySelector('.edit-ordercard-wrapper');
        orderCardUtils.store.createRecordView = createRecordView;
        createRecordView.style.display = 'none';

        const dropDown = createRecordView.querySelector('.ordercard-phones-dropdown-wrapper');
        dropDown.addEventListener('click', (event) => {
            const model = document.body.querySelector('.edit-record-phone');
            model.innerText = event.target.innerText;


            const phone = orderCardUtils.store.phones.find(
                (p) => p.model === event.target.innerText
            );
            document.body.querySelector('.edit-record-price').value = phone.price;

            dropDown.classList.add('display-none');
            event.preventDefault();
            event.stopImmediatePropagation();
        });

        createRecordView.querySelector('.edit-ordercard-phone')
            .addEventListener('click', function (event) {
                const dropDown = document.body.querySelector('.ordercard-phones-dropdown-wrapper');
                dropDown.classList.remove('display-none');
                const phones = document.body.querySelector('.ordercard-phones-dropdown');
                phones.innerHTML = '';
                orderCardUtils.store.phones.forEach((p) => {
                    phones.append(orderCardUtils.phone.createDropDownElement(p.model));
                });
                event.preventDefault();
                event.stopImmediatePropagation();
            });

        createRecordView.querySelector('.edit-ordercard-cancel-btn')
            .addEventListener('click', () => {
                orderCardUtils.spinner.fullScreenOverlay.style.display = 'none';
                orderCardUtils.store.createRecordView.style.display = 'none';
            });

        createRecordView.querySelector('.edit-ordercard-save-btn')
            .addEventListener('click', function () {
                orderCardUtils.spinner.fullScreenOverlay.style.display = 'none';
                orderCardUtils.store.createRecordView.style.display = 'none';
                const id = document.body.querySelector('.edit-record-id').value;
                const model = document.body.querySelector('.edit-record-phone');
                const phone = orderCardUtils.store.phones.find(
                    (p) => p.model === model.innerText
                );
                const itemCount = document.body.querySelector('.edit-record-count').value;
                const order = orderCardUtils.store.order;
                let orderCard = {
                    id: id,
                    itemCount,
                    order: order,
                    phone: phone
                };
                orderCardUtils.ajax.addOrderCard(orderCard)
                    .then(
                        (data) => {
                            orderCardUtils.orderCard.refreshTable();
                        })
                    .catch((err) => {
                        console.error(err)
                        showModal(err)
                    });
            });
        document.body.querySelector('.create-ordercard')
            .addEventListener('click', () => {
                orderCardUtils.orderCard.createNew(false);
            });
        document.body.querySelector('.refresh-table')
            .addEventListener('click', () => {
                orderCardUtils.orderCard.refreshTable();
            });
        document.body.querySelector('.back')
            .addEventListener('click', () => {
                orderCardUtils.ajax.backLink();
            });

        orderCardUtils.store.ordercardTable = document.querySelector('.ordercards-table-content');
        orderCardUtils.store.modal = document.getElementById("myModal");
        orderCardUtils.store.span = document.getElementsByClassName("close")[0];
    };


    orderCardUtils.spinner = {};
    orderCardUtils.spinner.spinnerContainerClassName = 'users-spinner-container';
    orderCardUtils.spinner.spinnerClassName = 'users-spinner';

    orderCardUtils.spinner.createSpinner = function () {
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
        orderCardUtils.spinner.spinnerContainer = spinnerContainer;
    };

    orderCardUtils.spinner.showSpinner = function () {
        this.fullScreenOverlay.style.display = '';
        this.spinnerContainer.style.display = '';
    };
    orderCardUtils.spinner.hideSpinner = function () {
        this.fullScreenOverlay.style.display = 'none';
        this.spinnerContainer.style.display = 'none';
    };


    orderCardUtils.ajax = {
        baseUrl: '',
        get ordercardsUrl() {
            return this.baseUrl + '/orderCards';
        },
        get ordersUrl() {
            return this.baseUrl + '/orders';
        },
        get phonesUrl() {
            return this.baseUrl + '/phones';
        },
        get accountsUrl() {
            return this.baseUrl + '/accounts';
        }
    };

    orderCardUtils.ajax.backLink = function () {
        location.href = `${this.accountsUrl}/${orderCardUtils.store.order.account.id}/info`;
    };

    orderCardUtils.ajax.getPhones = function () {
        orderCardUtils.spinner.showSpinner();
        orderCardUtils.store.phones = [];
        return window.axios
            .get(this.phonesUrl)
            .then((response) => {
                orderCardUtils.store.phones = response.data;
                orderCardUtils.spinner.hideSpinner();
            })
            .catch((err) => {
                console.error(err);
                showModal(err)
                orderCardUtils.spinner.hideSpinner();
            });
    };

    orderCardUtils.ajax.getOrderById = function () {
        orderCardUtils.spinner.showSpinner();
        let orderId = window.location.pathname.split('/')[2];
        return window.axios
            .get(`${this.ordersUrl}/${orderId}`)
            .then((response) => {
                orderCardUtils.store.order = response.data;
                orderCardUtils.spinner.hideSpinner();
            })
            .catch((err) => {
                console.error(err);
                orderCardUtils.store.order = null;
                showModal(err);
                orderCardUtils.spinner.hideSpinner();
            });
    };

    orderCardUtils.ajax.findOrderCardById = function (id) {
        return window.axios.get(`${this.ordercardsUrl}/${id}`)
    };

    orderCardUtils.ajax.getOrderCardsByOrderId = function () {
        orderCardUtils.spinner.showSpinner();
        orderCardUtils.store.orderCards = [];
        return window.axios
            .get(`${this.ordercardsUrl}/order/${orderCardUtils.store.order.id}`)
            .then((response) => {
                orderCardUtils.store.orderCards = response.data;
                orderCardUtils.spinner.hideSpinner();
            })
            .catch((err) => {
                console.error(err);
                showModal(err);
                orderCardUtils.spinner.hideSpinner();
            });
    };

    orderCardUtils.ajax.deleteOrderCardById = function (id) {
        return window.axios.delete(`${this.ordercardsUrl}/${id}`);
    };

    function showModal(err) {
        let modalMessage = orderCardUtils.store.modal.querySelector('.text');
        let errorData = err.response.data;
        if (errorData.hasOwnProperty("message")) {
            modalMessage.innerHTML = errorData.message;
        } else {
            modalMessage.innerHTML = JSON.stringify(errorData);
        }
        orderCardUtils.store.modal.style.display = "block";
        orderCardUtils.store.span.onclick = function () {
            orderCardUtils.store.modal.style.display = "none";
        }
    }

    orderCardUtils.ajax.saveOrderCard = function (orderCard) {
        return window.axios.post(`${this.ordercardsUrl}`, orderCard);
    };

    orderCardUtils.ajax.updateOrderCard = function (orderCard) {
        return window.axios.put(`${this.ordercardsUrl}/${orderCard.id}`, orderCard);
    };

    orderCardUtils.ajax.addOrderCard = function (orderCard) {
        return window.axios.post(`${this.ordersUrl}/${orderCardUtils.store.order.id}/add`, orderCard);
    };

    orderCardUtils.ajax.deleteOrderCard = function (orderCardId) {
        return window.axios.get(`${this.ordersUrl}/${orderCardUtils.store.order.id}/delete/${orderCardId}`);
    };

    orderCardUtils.orderCard = {};
    orderCardUtils.orderCard.refreshTable = function () {
        orderCardUtils.ajax.getPhones();
        orderCardUtils.ajax.getOrderById().then((data) => {
            orderCardUtils.ajax.getOrderCardsByOrderId().then((data) => {
                orderCardUtils.orderCard.cleanTable();
                orderCardUtils.orderCard.fillTable();
            })
        })
    };

    orderCardUtils.orderCard.edit = function (orderCard) {
        orderCardUtils.orderCard.createNew(true, orderCard);
    };

    orderCardUtils.orderCard.delete = function (orderCard) {
        orderCardUtils.spinner.showSpinner();
        orderCardUtils.ajax.deleteOrderCard(orderCard.id)
            .then((response) => {
                orderCardUtils.orderCard.refreshTable();
                orderCardUtils.spinner.hideSpinner();
            })
            .catch((err) => {
                console.error(err);
                showModal(err);
                orderCardUtils.spinner.hideSpinner();
            });
    };

    orderCardUtils.orderCard.createRecord = function (orderCard) {
        const viewTemplate = orderCardUtils.templates.record;
        viewTemplate.querySelector('.ordercard-id').innerText = orderCard.id;
        viewTemplate.querySelector('.ordercard-phone').innerText = orderCard.phone.model;
        viewTemplate.querySelector('.ordercard-count').innerText = orderCard.itemCount;
        viewTemplate.querySelector('.ordercard-price').innerText = orderCard.phone.price;
        let view = document.importNode(viewTemplate, true);
        view.orderCardId = orderCard.id;

        view.querySelector('.ordercard-edit-btn')
            .addEventListener('click', function (event) {
                orderCardUtils.orderCard.edit(orderCard)
            });
        view.querySelector('.ordercard-delete-btn')
            .addEventListener('click', function () {
                orderCardUtils.orderCard.delete(orderCard);
            });

        orderCardUtils.store.orderCardViews.push(view);
        return view;
    };

    orderCardUtils.orderCard.createNew = function (edit, orderCard) {
        orderCardUtils.store.createRecordView.style.display = '';
        orderCardUtils.spinner.fullScreenOverlay.style.display = '';
        const orderCardId = document.body.querySelector('.edit-record-id');
        const phone = document.body.querySelector('.edit-record-phone');
        const itemCount = document.body.querySelector('.edit-record-count');
        const price = document.body.querySelector('.edit-record-price');

        if (edit) {
            orderCardUtils.store.editOrderCard = orderCard;
            orderCardId.value = orderCard.id;
            phone.innerText = orderCard.phone.model;
            itemCount.value = orderCard.itemCount;
            price.value = orderCard.phone.price;
        } else {
            orderCardId.value = '';
            phone.innerText = '';
            itemCount.value = '';
            price.value = '';
            orderCardUtils.store.editOrderCard = null;
        }
    };

    orderCardUtils.orderCard.fillTable = function () {
        orderCardUtils.store.orderCards.forEach((oc) => {
            orderCardUtils.store.ordercardTable.append(orderCardUtils.orderCard.createRecord(oc));
        })
    };

    orderCardUtils.orderCard.cleanTable = function () {
        orderCardUtils.store.ordercardTable.innerHTML = '';
    };

    orderCardUtils.phone = {};
    orderCardUtils.phone.createDropDownElement = function createPhone(model) {
        const phone = document.createElement('div');
        phone.classList.add('ordercards-dropdown-phone');
        phone.innerText = model;
        return phone;
    };

    window.orderCardUtils = orderCardUtils;
}());
