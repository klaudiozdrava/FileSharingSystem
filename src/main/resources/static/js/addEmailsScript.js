<script>
    var fieldId = 0;

    function addElement(parentId, elementTag, elementId, html){
        var parent = document.getElementById(parentId);
        var newElement = document.createElement(elementTag);
        newElement.setAttribute('id', elementId);
        newElement.innerHTML = html;
        parent.appendChild(newElement);
    }

    function removeField(elementId){
        var element = document.getElementById(elementId);
        if (element) {
            element.parentNode.removeChild(element);
        }
    }

    function addField(){
        fieldId++;
        var html = '<div id="field-' + fieldId + '" class="form-group row align-items-center">'
        + '<label for="email-' + fieldId + '" class="col-sm-12 col-form-label">User email</label>'
        + '<div class="col-sm-10">'
        + '<input id="email-' + fieldId + '" type="email" name="email[]" class="form-control" placeholder="Enter user email">'
        + '</div>'
        + '<div class="col-sm-2">'
        + '<button type="button" onclick="removeField(\'field-' + fieldId + '\');" class="btn btn-danger"><span>X</span></button>'
        + '</div>'
        + '</div>';
        addElement('form-group', 'div', 'field-' + fieldId, html);
    }
</script>