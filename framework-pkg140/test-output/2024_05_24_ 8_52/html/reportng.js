function toggleElement(elementId, displayStyle) {
	var current = getStyle(elementId, 'display');
	document.getElementById(elementId).style.display = (current == 'none' ? displayStyle
			: 'none');
}

function toggleAllElement(elementId, displayStyle) {
	var current = getStyle(elementId, 'display');
	document.getElementById(elementId).style.display = (current == 'none' ? displayStyle
			: 'none');
}
/**
 * for result options check-box
 * 
 * @param obj
 * @return
 */
function toggleResult(obj) {
	var oldCls, newCls;
	if (obj.checked) {
		oldCls = obj.value + "hide";
		newCls = obj.value + "show";
	} else {
		oldCls = obj.value + "show";
		newCls = obj.value + "hide";
	}
	obj.parentNode.className = obj.parentNode.className.replace(oldCls, newCls);
}
function getStyle(elementId, property) {
	var element = document.getElementById(elementId);
	return element.currentStyle ? element.currentStyle[property]
			: document.defaultView.getComputedStyle(element, null)
					.getPropertyValue(property);
}
function toggleAllResult(obj) {
	var eleLst = document.getElementsByTagName("input");
	for ( var i = 0; i < eleLst.length; i++) {
		var ele = eleLst[i];
		if (ele.value == obj.value && ele.id != 'top-filter'
				&& ele.checked != obj.checked) {
			ele.click();
		}
	}
}

function toggle(toggleId) {
	var toggle;
	if (document.getElementById) {
		toggle = document.getElementById(toggleId);
	} else if (document.all) {
		toggle = document.all[toggleId];
	}
	toggle.textContent = toggle.innerHTML == '\u25b6' ? '\u25bc' : '\u25b6';
}
