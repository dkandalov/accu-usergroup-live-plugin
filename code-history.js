function createForcesGraphOn(elementId, graph, projectName, onNodeSelection, defaults) {
	function createQuickFindIndex(graph) {
		var quickFind = new QuickFind(graph.nodes.length);
		graph.links.forEach(function (link) {
			quickFind.connect(graph.nodes.indexOf(link.source), graph.nodes.indexOf(link.target));
		});
		return quickFind;
	}

	function GraphEditor(graph) {
		this.graph = graph;
		this.deletions = [];
		this.quickFind = createQuickFindIndex(graph);
	}
	GraphEditor.prototype.deleteLinks = function(linksToRemove) {
		this.deletions.push({links: linksToRemove});
		this.graph.links = this.graph.links.filter(function(link) { return linksToRemove.indexOf(link) == -1; });

		this.quickFind = createQuickFindIndex(this.graph);
	};
	GraphEditor.prototype.deleteNodes = function(nodes) {
		for (var i = 0; i < nodes.length; i++) {
			this.deleteNode(nodes[i]);
		}
		this.quickFind = createQuickFindIndex(this.graph);
	};
	GraphEditor.prototype.deleteNode = function(nodeToRemove) {
		var nodeIndex = this.graph.nodes.indexOf(nodeToRemove);
		var linksToRemove = this.graph.links.filter(function(link) { return link.source == nodeToRemove || link.target == nodeToRemove; });

		this.deletions.push({nodeIndex: nodeIndex, node: nodeToRemove, links: linksToRemove});

		this.graph.nodes = this.graph.nodes.filter(function(node) { return node != nodeToRemove; });
		this.graph.links = this.graph.links.filter(function(link) { return linksToRemove.indexOf(link) == -1; });
	};
	GraphEditor.prototype.undoAllDeletions = function () {
		while (this.deletions.length > 0) {
			var deletion = this.deletions.pop();

			if (deletion.node != null) {
				this.graph.nodes = this.graph.nodes.concat([deletion.node]);
				deletion.node.index = this.graph.nodes.length - 1;
			}

			this.graph.links = this.graph.links.concat(deletion.links);
		}
		this.quickFind = createQuickFindIndex(this.graph);
	};
	GraphEditor.prototype.nodesInOneClusterWith = function(node) {
		var quickFind = this.quickFind;
		return this.graph.nodes.filter(function(eachNode) {
			return quickFind.areConnected(eachNode.index, node.index);
		});
	};
	GraphEditor.prototype.immediateConnectionsOf = function(node) {
		var result = this.graph.links.map(function(link) {
			if (link.source == node) return link.target;
			else if (link.target == node) return link.source;
			else return null;
		}).filter(function(it) { return it != null; });
		result.push(node);
		return result;
	};

	var width = 1100,
		height = 700;

	var color = d3.scale.category20();
	var showLongFileNames = false;
	var gravityValues = {
		Low: -320,
		Medium: -120,
		High: -50
	};
	defaults = (defaults == null) ? {} : defaults;
	var gravity = (defaults.gravity == null ? gravityValues.Medium : gravityValues[defaults.gravity]);
	var minClusterSize = (defaults.minCluster == null ? 2 : defaults.minCluster);
	var linkValuesExtent = d3.extent(graph.links, function (link) { return link.value; });
	var minLinkStrength = (defaults.linkStrength == null ? linkValuesExtent[0] : defaults.linkStrength);
	var force = createForce(gravity);

	var rootElement = d3.select("#" + elementId);
	removeChildrenOf(elementId);

	var headerSpan = appendBlockElementTo(rootElement, width);
	headerSpan.append("h2").text(graphName + " (" + projectName + ")").style({"text-align": "center"});

	var svg = rootElement.append("svg").attr("width", width).attr("height", height);
	var svgLinks = svg.append("g");
	var svgNodes = svg.append("g");
	var svgPos = svg[0][0];

	svg.append("rect")
		.attr("width", width).attr("height", height)
		.style({fill: "none", stroke: "#AAAAAA"});

	force.nodes(graph.nodes).links(graph.links).start();
	var graphEditor = new GraphEditor(graph);


	var tooltip = rootElement.append("div")
		.attr("class", "tooltip")
		.style("opacity", 0);
	var selectedNodesLabel = rootElement.append("div")
		.attr("class", "selectedNodesLabel")
		.style("position", "absolute")
		.style("opacity", 0);
	var selectedNodesLabel2 = rootElement.append("div")
		.attr("class", "selectedNodesLabel")
		.style("position", "absolute")
		.style("opacity", 0);
	if (graph.nodes.length == 0)
		rootElement.append("div")
			.attr("class", "tooltip")
			.html("Unfortunately, there is nothing to show.")
			.style("position", "absolute")
			.style("top", function (){ return svgPos.offsetTop + (svgPos.offsetHeight / 2) - (this.offsetHeight / 2) + "px"; })
			.style("left", function (){ return svgPos.offsetLeft + (svgPos.offsetWidth / 2) - (this.offsetWidth / 2) + "px"; });

	var handledByNode = false;
	var onMouseOverNode = function (d) {
		tooltip.html(showLongFileNames ? d.name : dropPath(d.name))
			.style("opacity", .9)
			.style("position", "absolute")
			.style("left", svgPos.offsetLeft + d.x + 18 + "px")
			.style("top", svgPos.offsetTop + d.y - 18 + "px");
	};
	var onMouseOutOfNode = function() {
		tooltip.style("opacity", .0);
	};
	var whenClickedOnNode = function (node) {
		var shouldRemoveNode = d3.event.altKey && !d3.event.shiftKey;
		var shouldRemoveNodeCluster = d3.event.altKey && d3.event.shiftKey;

		if (shouldRemoveNode) {
			updateSelection([], showLongFileNames);
			tooltip.style("opacity", .0);

			graphEditor.deleteNodes([node]);

			linkElements = updateLinks(svgLinks, graph);
			nodeElements = updateNodes(svgNodes, force, graph, onMouseOverNode, onMouseOutOfNode, whenClickedOnNode);
			force.nodes(graph.nodes).links(graph.links).start();

		} else if (shouldRemoveNodeCluster) {
			updateSelection([], showLongFileNames);
			tooltip.style("opacity", .0);

			graphEditor.deleteNodes(graphEditor.nodesInOneClusterWith(node));

			linkElements = updateLinks(svgLinks, graph);
			nodeElements = updateNodes(svgNodes, force, graph, onMouseOverNode, onMouseOutOfNode, whenClickedOnNode);
			force.nodes(graph.nodes).links(graph.links).start();
		} else {
			var selectedNodes = onNodeSelection(graphEditor, node);
			updateSelection(selectedNodes, showLongFileNames);
		}

		handledByNode = true;
	};
	var linkElements = updateLinks(svgLinks, graph);
	var nodeElements = updateNodes(svgNodes, force, graph, onMouseOverNode, onMouseOutOfNode, whenClickedOnNode);


	svg.on("click", function () {
		if (handledByNode) {
			handledByNode = false;
			return;
		}
		updateSelection([], showLongFileNames);
	});

	var onForceTick = function () {
		linkElements
			.attr("x1", function(d) { return d.source.x; })
			.attr("y1", function(d) { return d.source.y; })
			.attr("x2", function(d) { return d.target.x; })
			.attr("y2", function(d) { return d.target.y; });
		nodeElements
			.attr("cx", function(d) { return d.x; })
			.attr("cy", function(d) { return d.y; });
	};
	force.on("tick", onForceTick);


	var footerSpan = appendBlockElementTo(rootElement, width);
	var span = footerSpan.append("span").style({float: "right"});
	span.append("label").html("Show file path:");
	span.append("input").attr("type", "checkbox")
		.on("click", function() { showLongFileNames = !showLongFileNames; });
	span.call(addDelimiter);

	span.append("label").html("Gravity: ");
	var gravityDropDown = span.append("select");
	for (var attribute in gravityValues) {
		var option = gravityDropDown.append("option").attr("value", attribute).html(attribute);
		if (gravityValues[attribute] == gravity) option.attr("selected", "selected");
	}

	gravityDropDown.on("change", function () {
		gravity = gravityValues[this.value];

		force = createForce(gravity);
		force.on("tick", onForceTick);
		force.nodes(graph.nodes).links(graph.links).start();
		svg.selectAll(".node").call(force.drag);
	});
	span.call(addDelimiter);

	span.append("label").html("Min cluster size: ");
	var clusterSizeDropDown = span.append("select");
	d3.range(2, 11).forEach(function (i) {
		var option = clusterSizeDropDown.append("option").attr("value", i).html(i);
		if (minClusterSize == i) option.attr("selected", "selected");
	});
	clusterSizeDropDown.on("change", function () {
		minClusterSize = +this.value;
		filterNodes();
	});
	span.call(addDelimiter);

	span.append("label").html("Min link strength: ");
	var linkStrengthDropDown = span.append("select");
	d3.range(linkValuesExtent[0], linkValuesExtent[1]).forEach(function (i) {
		var option = linkStrengthDropDown.append("option").attr("value", i).html(i);
		if (minLinkStrength == i) option.attr("selected", "selected");
	});
	linkStrengthDropDown.on("change", function() {
		minLinkStrength = +this.value;
		filterNodes();
	});


	var subFooterSpan = appendBlockElementTo(rootElement, width);
	subFooterSpan.append("span").style({float: "left"})
		.append("div").style("color", "#999")
		.html("alt+click or alt+shift+click to remove node or group of nodes");


	filterNodes();


	function createForce(gravity) {
		return d3.layout.force().charge(gravity).linkDistance(30).size([width, height]);
	}

	function updateNodes(svg, force, graph, onMouseOverNode, onMouseOutOfNode, whenClickedOnNode) {
		svg.selectAll(".node")
			.data(graph.nodes, byNodeName)
			.enter().append("circle")
			.attr("class", "node")
			.attr("r", function(d){ return d.selected ? 7 : 5; })
			.style("fill", function (d) { return color(d.group); })
			.call(force.drag)
			.on("mouseover", onMouseOverNode)
			.on("mouseout", onMouseOutOfNode)
			.on("click", whenClickedOnNode);
		svg.selectAll(".node")
			.data(graph.nodes, byNodeName)
			.exit().remove();
		return svg.selectAll(".node");
	}

	function updateLinks(svg, graph) {
		svg.selectAll(".link")
			.data(graph.links, byLinkState)
			.enter().append("line")
			.attr("class", "link")
			.style("stroke-width", function (d) { return Math.sqrt(d.value); });
		svg.selectAll(".link")
			.data(graph.links, byLinkState)
			.exit().remove();
		return svg.selectAll(".link");
	}

	function filterNodes() {
		updateSelection([], showLongFileNames);
		tooltip.style("opacity", .0);

		graphEditor.undoAllDeletions();
		graphEditor.deleteLinks(linksWithStrengthLessThan(minLinkStrength, graph));
		graphEditor.deleteNodes(nodesInClustersSmallerThan(minClusterSize, graph, graphEditor));

		linkElements = updateLinks(svgLinks, graph);
		nodeElements = updateNodes(svgNodes, force, graph, onMouseOverNode, onMouseOutOfNode, whenClickedOnNode);
		force.nodes(graph.nodes).links(graph.links).start();
	}

	function linksWithStrengthLessThan(minLinkStrength, graph) {
		return graph.links.filter(function (link) { return link.value < minLinkStrength; })
	}

	function nodesInClustersSmallerThan(maxClusterSize, graph, graphEditor) {
		var visitedNodes = [];
		var nodes = [];
		graph.nodes.forEach(function (node) {
			if (visitedNodes.indexOf(node) >= 0) return;

			var connectedNodes = graphEditor.nodesInOneClusterWith(node);
			if (connectedNodes.length < maxClusterSize) {
				nodes = nodes.concat(connectedNodes);
			}
			visitedNodes = visitedNodes.concat(connectedNodes);
		});
		return nodes;
	}

	function updateSelection(selectedNodes, showLongFileNames) {
		graph.nodes.forEach(function (eachNode) { eachNode.selected = false; });
		selectedNodes.forEach(function (eachNode) { eachNode.selected = true; });
		nodeElements.data(graph.nodes, byNodeName).attr("r", function(d){ return d.selected ? 7 : 5; });

		var nodesByGroup = d3.nest().key(function (node) { return node.group; }).map(selectedNodes);
		if (nodesByGroup[1] == null) nodesByGroup[1] = [];
		if (nodesByGroup[2] == null) nodesByGroup[2] = [];

		selectedNodesLabel
			.style("left", svgPos.offsetLeft + 3 + "px")
			.style("top", svgPos.offsetTop + 3 + "px")
			.html(nodesByGroup[1].map(function (d) { return showLongFileNames ? d.name : dropPath(d.name); }).sort().join("<br/>"))
			.style("opacity", (nodesByGroup[1].length > 0 ? 1 : 0));
		var pos = selectedNodesLabel[0][0];
		selectedNodesLabel2
			.style("left", svgPos.offsetLeft + 3 + "px")
			.style("top", pos.offsetTop + pos.offsetHeight + 3 + "px")
			.html(nodesByGroup[2].map(function (d) { return showLongFileNames ? d.name : dropPath(d.name); }).sort().join("<br/>"))
			.style("opacity", (nodesByGroup[2].length > 0 ? 1 : 0));
	}

	function dropPath(fileName) {
		var i = fileName.lastIndexOf("/");
		return (i == -1) ? fileName : fileName.substring(i + 1)
	}

	function appendBlockElementTo(element, width) {
		return element.append("span").style({display: "block", width: width + "px"});
	}

	function addDelimiter(span) {
		span.append("span").style({width: "20px", display: "inline-block"});
	}

	function byNodeName(node) { return node.name; }
	function byLinkState(link) { return "" + link.source.index + "-" + link.target.index; }

	function removeChildrenOf(elementId) {
		var element = document.getElementById(elementId);
		while (element.children.length > 0) {
			var child = element.children.item(0);
			child.parentNode.removeChild(child);
		}
	}
}


function QuickFind(size) {
	this.size = size;
	this.connections = new Array(size);
	for (var i = 0; i < this.connections.length; i++) {
		this.connections[i] = i;
	}
}
QuickFind.prototype.areConnected = function(p1, p2) {
	return this.connections[p1] == this.connections[p2];
};
QuickFind.prototype.connect = function(p1, p2) {
	var p1Root = this.rootOf(this.connections[p1]);
	var p2Root = this.rootOf(this.connections[p2]);
	for (var i = 0; i < this.connections.length; i++) {
		if (this.connections[i] == p1Root) this.connections[i] = p2Root;
	}
};
QuickFind.prototype.rootOf = function(p) {
	return this.connections[p] == p ? p : this.rootOf(this.connections[p]);
};


// this is intended for projector in case default colors are too pale to see
function enableDarkColorsShortcut() {
	document.onkeydown = function(e) {
		e = window.event || e;
		if (String.fromCharCode(e.keyCode) == 'D') {
			d3.selectAll(".link")[0].forEach(function(link) {
				link.style["stroke"] = "#333000";
				link.style["stroke-opacity"] = 0.8;
			});
			d3.selectAll(".node")[0].forEach(function(node) {
				if (node.style["fill"] == "#1f77b4") node.style["fill"] = "#1033a2";
				else if (node.style["fill"] == "#aec7e8") node.style["fill"] = "#cc6666";
			});
		}
	};
}
enableDarkColorsShortcut();