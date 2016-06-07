(function () {
    'use strict';

    // Вызов разных методов при инициилизации
    initHandlers();

    // Инициилизация обработчиков событий
    function initHandlers() {
        $('#toggle-all-books').on('change', toggleAllBooks);
        $('.chk-select-book').on('change', checkMainToggle);
        $('#toggle-all-books, .chk-select-book').on('change', actionsToggle);
    }
    
    // Переключает все checkbox'ы с выбранными книгами
    function toggleAllBooks() {
        var isChecked = $('#toggle-all-books').is(':checked');
        $('.chk-select-book').prop('checked', isChecked);
    }
    
    // Проверяет и устанавливает состояние "главного" флажка
    function checkMainToggle() {
        var isMainToggleChecked = true;
        
        /* Если хоть один из флажков сброшен - сбрасываем и "главный".
         * Если же все флажки выбраны - выбираем и "главный".
         */
        $('.chk-select-book').each(function(index, elem) {
            if (!elem.checked) {
                isMainToggleChecked = false;
                return false;
            }
        });

        $('#toggle-all-books').prop('checked', isMainToggleChecked);
    }
    
    // Включает/выключает выбор действий
    function actionsToggle() {
        var isActionsDisabled = true;
        
        $('.chk-select-book').each(function(index, elem) {
            if (elem.checked) {
                isActionsDisabled = false;
                return false;
            }
        });

        $('#actions-select').prop('disabled', isActionsDisabled);
    }

})();
