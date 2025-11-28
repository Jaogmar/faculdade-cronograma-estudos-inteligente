// App.js - Funcionalidades JavaScript globais

console.log('Planner de Estudos - Sistema iniciado');

// Função para mostrar loading
function showLoading(buttonElement, text = 'Carregando...') {
    if (buttonElement) {
        buttonElement.disabled = true;
        buttonElement.innerHTML = `<span class="animate-spin inline-block mr-2">⏳</span> ${text}`;
    }
}

// Função para esconder loading
function hideLoading(buttonElement, originalText) {
    if (buttonElement) {
        buttonElement.disabled = false;
        buttonElement.innerHTML = originalText;
    }
}

// Auto-hide mensagens de sucesso/erro após 5 segundos
document.addEventListener('DOMContentLoaded', function() {
    const mensagens = document.querySelectorAll('.bg-green-100, .bg-red-100');
    mensagens.forEach(function(mensagem) {
        setTimeout(function() {
            mensagem.style.transition = 'opacity 0.5s';
            mensagem.style.opacity = '0';
            setTimeout(function() {
                mensagem.remove();
            }, 500);
        }, 5000);
    });
});
