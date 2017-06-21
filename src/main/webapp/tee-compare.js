window.onload = drawAll;
window.onresize = setMaxCodeArea;

var navigation = true;
var maxHeightForNoNavigation = 300;

/**
 * Adds row numbers to blocks with SQL code and draws connectors between pairs
 * of differences
 * 
 */
function drawAll() {
	setMaxCodeArea();
	drawSeparateLineBlocks();
	drawConnectors();
	modifyStyles();
}

function modifyStyles() {
	var mainPanel = document.getElementById("main-panel");
	mainPanel.style["margin-left"] = "0px";
}

function drawSeparateLineBlocks() {
	var aboveLineDiffs = document.querySelectorAll(".aboveLine");
	var underLineDiffs = document.querySelectorAll(".underLine");

	if (aboveLineDiffs.length > 0) {
		for (var i = aboveLineDiffs.length - 1; i >= 0; i--) {
			var pairNumber = aboveLineDiffs[i].className.substr(
					aboveLineDiffs[i].className.indexOf("pair") + 4, 5);

			var line = getClosest(aboveLineDiffs[i], "line");
			var diff = document.createElement("DIV");

			if (aboveLineDiffs[i].className.indexOf("source") != -1) { // Source
																		// difference
				diff.className = "block source missing separate pair"
						+ pairNumber;
			} else { // Target
				diff.className = "block target missing separate pair"
						+ pairNumber;
			}

			line.parentNode.insertBefore(diff, line);

			aboveLineDiffs[i].parentNode.removeChild(aboveLineDiffs[i]);
		}
	}

	if (underLineDiffs.length > 0) {
		for (var i = underLineDiffs.length - 1; i >= 0; i--) {
			var pairNumber = underLineDiffs[i].className.substr(
					underLineDiffs[i].className.indexOf("pair") + 4, 5);

			var line = getClosest(underLineDiffs[i], "line");
			var diff = document.createElement("DIV");

			if (underLineDiffs[i].className.indexOf("source") != -1) { // Source
																		// difference
				diff.className = "block source missing separate pair"
						+ pairNumber;
			} else { // Target
				diff.className = "block target missing separate pair"
						+ pairNumber;
			}

			line.parentNode.insertBefore(diff, line.nextSibling);

			underLineDiffs[i].parentNode.removeChild(underLineDiffs[i]);
		}
	}
}

function getClosest(el, classname) {
	do {
		if (el.className.indexOf(classname) != -1) {
			return el;
		}
	} while (el = el.parentNode);

	return null;
}

/**
 * Gets positions and heights of paired elements and draws SVG path by setting
 * the "d" attribute to correct mid height values.
 */
function drawConnectors() {
	var paths = document.getElementsByTagName("path");
	var dTable = document.querySelectorAll(".dTable");

	if (dTable.length > 0) {
		var dTableBox = dTable[0].getBoundingClientRect();

		for (var i = 0; i < paths.length; i++) {
			var pairNumber = paths[i].className.baseVal.substr(
					paths[i].className.baseVal.indexOf("pair") + 4, 5);
			var sourceBlocks = document.querySelectorAll(".left div.pair"
					+ pairNumber);
			var targetBlocks = document.querySelectorAll(".right div.pair"
					+ pairNumber);

			var sourceStartBlock = sourceBlocks[0];
			var sourceEndBlock = sourceBlocks[1];
			var targetStartBlock = targetBlocks[0];
			var targetEndBlock = targetBlocks[1];

			var startOffsetsSource = sourceStartBlock.getBoundingClientRect();
			var endOffsetsSource = sourceEndBlock ? sourceEndBlock
					.getBoundingClientRect() : sourceStartBlock
					.getBoundingClientRect();
			var source = (startOffsetsSource.top
					+ (endOffsetsSource.bottom - startOffsetsSource.top) / 2 - sourceStartBlock.scrollTop)
					- dTableBox.top;

			var startOffsetsTarget = targetStartBlock.getBoundingClientRect();
			var endOffsetsTarget = targetEndBlock ? targetEndBlock
					.getBoundingClientRect() : targetStartBlock
					.getBoundingClientRect();
			var target = (startOffsetsTarget.top
					+ (endOffsetsTarget.bottom - startOffsetsTarget.top) / 2 - targetStartBlock.scrollTop)
					- dTableBox.top;

			var connector = document.querySelector("path.pair" + pairNumber);
			connector.setAttribute("d", "M 0 " + source + " C 10 " + source
					+ ", 50 " + target + ", 60 " + target); // SVG property

			var sourceLine = document.querySelector("line.source.pair"
					+ pairNumber);

			var targetLine = document.querySelector("line.target.pair"
					+ pairNumber);

			sourceLine.setAttribute('x1', 0);
			sourceLine.setAttribute('y1', startOffsetsSource.top
					- sourceStartBlock.scrollTop - dTableBox.top);
			sourceLine.setAttribute('x2', 0);
			sourceLine.setAttribute('y2', endOffsetsSource.bottom
					- sourceStartBlock.scrollTop - dTableBox.top);
			sourceLine.setAttribute('stroke-width', 2);

			targetLine.setAttribute('x1', 60);
			targetLine.setAttribute('y1', startOffsetsTarget.top
					- targetStartBlock.scrollTop - dTableBox.top);
			targetLine.setAttribute('x2', 60);
			targetLine.setAttribute('y2', endOffsetsTarget.bottom
					- targetStartBlock.scrollTop - dTableBox.top);
			targetLine.setAttribute('stroke-width', 2);
		}
	}
}

/**
 * Clears all existing highlighted pairs and then selects clicked object plus
 * its pair and sets css class to the pair. This function is called from HTML
 * element via event spec: onclick = "highlight(this)".
 * 
 * @param {Object}
 *            object - passed object "this"
 */
function highlightPair(object) {
	// Gets pair number
	var pairNumber = object.className.substr(
			object.className.indexOf("pair") + 4, 5);

	// Clear existing highlighted items
	var toClear = document.querySelectorAll(".highlighted");
	for (var i = 0; i < toClear.length; i++) {
		try {
			toClear[i].classList.remove('highlighted');
			toClear[i].classList.remove('first');
			toClear[i].classList.remove('last');
		} catch (ex) {

			var cName = toClear[i].className.replace(' highlighted', ''); // IE
			cName = toClear[i].className.replace(' last', '');
			cName = toClear[i].className.replace(' first', '');
			toClear[i].className = cName; // IE
		}

	}

	// Selects all items that belong to the pair
	var el = document.querySelectorAll(".pair" + pairNumber);

	// Adds css class .highlighted where necessary
	for (var i = 0; i < el.length; i++) {
		try {
			el[i].classList.add('highlighted');
		} catch (ex) {
			el[i].className += ' highlighted'; // IE
		}
	}

	var sourceSpans = document.querySelectorAll("span.source.pair" + pairNumber);
	var targetSpans = document.querySelectorAll("span.target.pair" + pairNumber);

	addFirstLastClassesToSpans(sourceSpans);
	addFirstLastClassesToSpans(targetSpans);

	drawConnectors();
}

function addFirstLastClassesToSpans(spans) {
	if (spans && spans.length > 0) {
		try {
			spans[0].classList.add('first');
			spans[spans.length - 1].classList.add('last');
		} catch (ex) {
			spans[0].className += ' first';
			spans[spans.length - 1].className += ' last';
		}
	}
}

/**
 * Gets available height and passes the value to setBoxHeight function
 */
function setMaxCodeArea() {
	if (navigation === false) {
		setBoxHeight(maxHeightForNoNavigation, ".res");
		setBoxHeight(maxHeightForNoNavigation + 20, ".leftNavig");
	} else {
		setBoxHeight(getAvailableHeight(".dTable"), ".res");
		setBoxHeight(getAvailableHeight(".leftNavig") - 10, ".leftNavig");
	}
}

/**
 * Sets height in pixels to all elemensts defined by selector property
 * 
 * @param {Number}
 *            heightPx - height in pixels
 * @param {String}
 *            selector - css selector, for example .class, #id etc.
 */
function setBoxHeight(heightPx, selector) {
	var boxes = document.querySelectorAll(selector);
	for (var i = 0; i < boxes.length; i++) {
		boxes[i].style.height = heightPx + "px";
	}
}

/**
 * Function is used for getting available height for displaying blocks with SQL
 * content. When window is resized, the available height must be re-calculated.
 * 
 * @param {String}
 *            selector - css, for example ".dTable"
 * @returns {Number} - available height (parent element height minus top
 *          position of the div).
 */
function getAvailableHeight(selector) {
	var totalHeight = 0;
	var height = window.innerHeight || document.documentElement.clientHeight
			|| document.body.clientHeight;

	var elements = document.querySelectorAll(selector);
	if (elements[0]) {
		var box = elements[0].getBoundingClientRect();
		var boxTop = box.top;
		totalHeight += +boxTop;
	}
	return height - totalHeight - 2;
}