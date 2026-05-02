document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('checkForm');
    const yInput = document.getElementById('y');
    const rInput = document.getElementById('r');
    
    function validateInput(input) {
        const value = input.value.trim();
        
        if (value === '') {
            return { isValid: false, message: 'Поле не может быть пустым' };
        }
        
        if (isNaN(value)) {
            return { isValid: false, message: 'Введите корректное число' };
        }
        
        const numValue = parseFloat(value);
        
        if (input.name === 'y') {
            if (numValue < -5 || numValue > 5) {
                return { isValid: false, message: 'Значение Y должно быть в диапазоне от -5 до 5' };
            }
        }
        
        if (input.name === 'r') {
            if (numValue < 1 || numValue > 4) {
                return { isValid: false, message: 'Значение R должно быть в диапазоне от 1 до 4' };
            }
        }
        
        return { isValid: true, message: '' };
    }
    
    function validateForm() {
        let allValid = true;
        
        [yInput, rInput].forEach(input => {
            const validation = validateInput(input);
            if (!validation.isValid) {
                allValid = false;
                input.style.borderColor = '#dc3545';
                input.style.backgroundColor = '#fff5f5';
                input.title = validation.message;
            } else {
                input.style.borderColor = '#28a745';
                input.style.backgroundColor = '#f8fff8';
                input.title = '';
            }
        });
        
        const xInput = document.getElementById('x');
        if (!xInput.value) {
            allValid = false;
            document.querySelector('.button-group').style.border = '2px solid #dc3545';
            document.querySelector('.button-group').style.padding = '5px';
            document.querySelector('.button-group').style.borderRadius = '5px';
        } else {
            document.querySelector('.button-group').style.border = 'none';
            document.querySelector('.button-group').style.padding = '0';
        }
        
        return allValid;
    }
    
    [yInput, rInput].forEach(input => {
        input.addEventListener('input', validateForm);
        input.addEventListener('blur', function() {
            const validation = validateInput(this);
            if (!validation.isValid && this.value.trim() !== '') {
                this.style.borderColor = '#dc3545';
                this.style.backgroundColor = '#fff5f5';
                this.title = validation.message;
            }
        });
        input.addEventListener('focus', function() {
            this.style.borderColor = '#3498db';
            this.style.backgroundColor = '#f8fff8';
        });
    });
    
    form.addEventListener('submit', function(e) {
        console.log('=== Form submit event ===');
        
        // Удаляем старые временные поля fromCanvas
        const existingCanvasFields = document.querySelectorAll('input[name="fromCanvas"][id="tempCanvasField"]');
        existingCanvasFields.forEach(field => field.remove());
        
        // Определяем значение fromCanvas
        let fromCanvasValue = 'false'; // по умолчанию для кнопки отправки
        
        // Проверяем, был ли клик по canvas (в canvas скрипте)
        const canvasClickField = document.querySelector('input[name="fromCanvas"][value="true"]');
        if (canvasClickField) {
            fromCanvasValue = 'true';
            console.log('Form submitted from canvas click');
        } else {
            console.log('Form submitted from submit button');
        }
        
        // Добавляем параметр fromCanvas
        const canvasField = document.createElement('input');
        canvasField.type = 'hidden';
        canvasField.name = 'fromCanvas';
        canvasField.value = fromCanvasValue;
        canvasField.id = 'tempCanvasField';
        form.appendChild(canvasField);
        
        console.log('Added fromCanvas parameter: ' + fromCanvasValue);
        
        // Логируем все данные формы
        const formData = new FormData(form);
        console.log('Form data before submit:');
        for (let [key, value] of formData.entries()) {
            console.log(key + ': ' + value);
        }
        
        // Только визуальная проверка
        validateForm();
    });
    
    document.querySelectorAll('.x-btn').forEach(button => {
        button.addEventListener('click', function() {
            document.querySelectorAll('.x-btn').forEach(btn => {
                btn.classList.remove('active');
            });
            this.classList.add('active');
            document.getElementById('x').value = this.getAttribute('data-value');
            validateForm();
        });
    });
    
    validateForm();
});