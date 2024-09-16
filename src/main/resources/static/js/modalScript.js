
<script>
    function showModal(event) {
        event.stopPropagation();  // Prevent the event from bubbling up to the <tr> element
        var modal =  document.getElementById("shareModal");
        $(modal).modal('show');
    }
    function closeModal() {
      var modal =  document.getElementById("shareModal");
      const inputs = modal.querySelectorAll('input');
      inputs.forEach(input => input.value = '');
      $(modal).modal('hide');
    }
</script>