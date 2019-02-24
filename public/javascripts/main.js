
function switchCheckBox(element) {
    if (element.checked === 'checked') {
        element.value = 'false';
    } else {
        element.value = 'true';
    }
}