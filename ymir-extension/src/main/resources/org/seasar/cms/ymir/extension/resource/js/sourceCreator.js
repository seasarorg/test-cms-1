Event.observe(window, 'load', prepareForInPlaceEditing, false);

function prepareForInPlaceEditing() {
    var bodyTags = $A(document.getElementsByTagName('body'));
    if (bodyTags.length == 0) {
        return;
    }

    var bodyTag = bodyTags.first();

    new SourceCreator.InPlaceEditor(
        bodyTag,
        '@CONTEXT_PATH@/__ymir__/updateTemplate.do',
        {
            cols: 80,
            rows: 25,
            highlightcolor: '#FFFFFF',
            clickToEditText: 'Double click to edit the template',
            loadTextURL: '@CONTEXT_PATH@/__ymir__/editTemplate.do?path='
                + encodeURIComponent(location.href),
            callback: function (form, value) {
                return 'path=' + encodeURIComponent(location.href)
                    + '&body=' + encodeURIComponent(value);
            },
            onComplete: function (transport, element) {
                window.location.reload();
            }
        }
    );
}

var SourceCreator = {};

SourceCreator.InPlaceEditor = Class.create();

Object.extend(SourceCreator.InPlaceEditor.prototype, Ajax.InPlaceEditor.prototype);
Object.extend(SourceCreator.InPlaceEditor.prototype, {
  initialize: function(element, url, options) {
    Ajax.InPlaceEditor.prototype.initialize.apply(this, arguments);

    Event.stopObserving(this.element, 'click', this.onclickListener);
    Event.observe(this.element, 'dblclick', this.onclickListener);
    if (this.options.externalControl) {
      Event.stopObserving(this.options.externalControl,
        'click', this.onclickListener);
      Event.observe(this.options.externalControl,
        'dblclick', this.onclickListener);
    }
  },
  dispose: function () {
    Ajax.InPlaceEditor.prototype.dispose.apply(this, arguments);

    Event.stopObserving(this.element, 'dblclick', this.onclickListener);
    if (this.options.externalControl) {
      Event.stopObserving(this.options.externalControl,
        'dblclick', this.onclickListener);
    }
  },
  onLoadedExternalText: function(transport) {
    Element.removeClassName(this.form, this.options.loadingClassName);
    this.editField.disabled = false;
    this.editField.value = transport.responseText;
    Field.scrollFreeActivate(this.editField);
  }
});
