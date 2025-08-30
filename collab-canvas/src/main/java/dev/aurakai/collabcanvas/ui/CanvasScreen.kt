package dev.aurakai.collabcanvas.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.aurakai.collabcanvas.model.CanvasElement
import dev.aurakai.collabcanvas.model.ElementType
import dev.aurakai.collabcanvas.ui.animation.*
import kotlinx.coroutines.launch

/**
 * Displays a collaborative drawing canvas with multi-tool support, gesture handling, and animated path rendering.
 */
/**
 * An interactive composable screen that provides a collaborative drawing canvas with multi-tool support.
 *
 * Supports freehand path drawing, basic rectangle and oval elements, pinch-to-zoom, panning, and progressive
 * animated rendering of previously drawn paths. The UI includes a top app bar (clear and save actions),
 * floating tool buttons to select Path/Rectangle/Oval, and a toolbar for color, stroke width selection, and clearing.
 *
 * State managed by this composable:
 * - A list of completed drawable paths (with animated copies for progressive rendering).
 * - A list of canvas elements (path/rectangle/oval).
 * - Current in-progress Path, current color, stroke width, selected tool/element, and an isDrawing flag.
 * - Animatables for zoom (scale) and pan (offset), and a transformable gesture state for handling pinch/drag gestures.
 *
 * Side effects and notable behavior:
 * - Mutates internal state lists (paths, elements, animatedPaths) as the user draws or clears the canvas.
 * - Pinch-to-zoom and pan update the internal scale and offset animatables.
 * - The top-bar "Clear Canvas" and toolbar "Clear" actions remove stored paths and animated paths.
 * - The "Save" action is a placeholder and does not persist the canvas in this implementation.
 */

/**
 * A composable full-screen collaborative drawing canvas with multi-tool support, pan/zoom gestures, and animated replay of completed strokes.
 *
 * This function hosts the UI and state for an interactive drawing surface:
 * - Maintains in-progress drawing state (current path, color, stroke width, selected tool).
 * - Stores completed drawable items in `paths` and `elements`, and keeps per-path animated copies in `animatedPaths` for progressive rendering.
 * - Supports three tools (path, rectangle, oval) selectable via floating action buttons; freehand drawing is performed when the PATH tool is active.
 * - Handles pinch-to-zoom and pan via a transformable state (`scale` and `offset`) and adjusts pointer interactions so strokes and toolbar behave consistently under zoom.
 * - Detects tap/drag gestures to begin, update, and finish drawings; completed paths are added to `paths`.
 * - Renders a background grid, persisted elements, the current in-progress path (with stroke scaled to remain visually consistent while zoomed), and animated playback of previous paths.
 * - Exposes UI actions to clear the canvas (removes `paths`, `elements`, and `animatedPaths`) and a placeholder save action.
 *
 * This composable does not return a value; it manages its own internal Compose state and side effects and composes child UI such as `CanvasToolbar`.
 */
/**
 * Full-screen, interactive collaborative drawing canvas composable.
 *
 * Provides a pinch-to-zoom and pan-enabled drawing surface with multiple tools (freehand/path,
 * rectangle, oval), color and stroke-width selection, animated replay of completed strokes,
 * and controls to clear or save the canvas. Maintains local UI state for:
 * - completed drawable items (paths and persisted elements),
 * - the in-progress Path, selected tool, color, stroke width, and drawing flag,
 * - animated copies used for progressive replay,
 * - zoom scale and pan offset as Animatable states.
 *
 * User interactions:
 * - Tap/drag gestures draw a freehand path when the PATH tool is selected.
 * - Drag gestures start, extend, and finish the current path; finished non-empty paths are
 *   appended to the internal `paths` list and also copied into `animatedPaths` for replay.
 * - Floating action buttons switch tools; the toolbar controls color, stroke width, and clearing.
 *
 * Rendering:
 * - Draws a background grid, persisted canvas elements (paths, rectangles, ovals), the current
 *   in-progress path (scaled to remain visually consistent under zoom), and animated paths
 *   (each with its own scale, offset, and alpha).
 *
 * Side effects:
 * - Mutates local remembered state lists/maps: `paths`, `elements`, and `animatedPaths`.
 * - Updates `scale` and `offset` Animatable states in response to transform gestures.
 *
 * Note: This composable is UI/stateful and intended to be used directly within a Compose hierarchy;
 * it does not persist canvas contents outside its in-memory state.
 */
/**
 * Full-screen collaborative drawing canvas with multi-tool drawing, pan/zoom, and per-path animation.
 *
 * Provides an interactive, stateful drawing surface that supports freehand (path), rectangle, and oval tools;
 * pinch-to-zoom and panning; an animated replay layer for completed strokes; and UI controls for tool selection,
 * color, stroke width, clearing, and (placeholder) saving. All drawing state (current stroke, completed paths,
 * persisted elements, animated copies, transform state) is held in memory within the composable.
 *
 * Notes:
 * - Freehand strokes are captured via drag gestures; the visible stroke width of the in-progress path is kept
 *   visually consistent while zooming.
 * - Completed strokes are stored as PluckablePath instances and have animated copies used for replay rendering.
 * - Persisted CanvasElement entries (paths, rectangles, ovals) are rendered from their stored Path and style.
 * - Clear actions remove in-memory paths/elements/animated copies. This composable does not perform external
 *   persistence.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CanvasScreen() {
    // Canvas state
    val paths = remember { mutableStateListOf<PluckablePath>() }
    val elements = remember { mutableStateListOf<CanvasElement>() }
    var currentPath by remember { mutableStateOf(Path()) }
    var currentColor by remember { mutableStateOf(Color.Black) }
    var strokeWidth by remember { mutableStateOf(5f) }
    var selectedTool by remember { mutableStateOf<ElementType>(ElementType.PATH) }
    var isDrawing by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    rememberScrollState()

    // Animation states
    val animatedPaths = remember { mutableStateMapOf<Int, PluckablePath>() }
    val scale = remember { Animatable(1f) }
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    // Update animated paths when paths change
    LaunchedEffect(paths) {
        paths.forEachIndexed { index, path ->
            if (!animatedPaths.containsKey(index)) {
                animatedPaths[index] = path.copy()
            }
        }
    }

    // Canvas gesture handlers
    val panZoomState = rememberTransformableState { zoomChange, panChange, rotationChange ->
        coroutineScope.launch {
            scale.snapTo(scale.value * zoomChange)
            val newOffset = offset.value + panChange * (1f / scale.value)
            offset.snapTo(newOffset)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Co-lab Canvas") },
                actions = {
                    IconButton(onClick = { 
                        paths.clear()
                        elements.clear()
                        animatedPaths.clear()
                    }) {
                        Icon(Icons.Default.Delete, "Clear Canvas")
                    }
                    IconButton(onClick = { /* Save canvas */ }) {
                        Icon(Icons.Default.Check, "Save")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tool selection buttons
                FloatingActionButton(
                    onClick = { selectedTool = ElementType.PATH },
                    modifier = Modifier.size(48.dp),
                    containerColor = if (selectedTool == ElementType.PATH) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(Icons.Default.Edit, "Draw")
                }
                FloatingActionButton(
                    onClick = { selectedTool = ElementType.RECTANGLE },
                    modifier = Modifier.size(48.dp),
                    containerColor = if (selectedTool == ElementType.RECTANGLE) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(Icons.Default.CropSquare, "Rectangle")
                }
                FloatingActionButton(
                    onClick = { selectedTool = ElementType.OVAL },
                    modifier = Modifier.size(48.dp),
                    containerColor = if (selectedTool == ElementType.OVAL) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(Icons.Default.Circle, "Circle")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { tapOffset ->
                            isDrawing = true
                            currentPath.moveTo(tapOffset.x, tapOffset.y)
                            tryAwaitRelease()
                            isDrawing = false
                        }
                    )
                }
        ) {
            // Main drawing canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(panZoomState)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { dragOffset ->
                                if (selectedTool == ElementType.PATH) {
                                    isDrawing = true
                                    currentPath.moveTo(dragOffset.x, dragOffset.y)
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                when (selectedTool) {
                                    ElementType.PATH -> {
                                        val x = change.position.x
                                        val y = change.position.y
                                        currentPath.lineTo(x, y)
                                    }
                                    else -> {
                                        // Handle other element types
                                    }
                                }
                            },
                            onDragEnd = {
                                isDrawing = false
                                // Add the completed path to the list
                                if (currentPath.getBounds().isEmpty.not()) {
                                    paths.add(PluckablePath(currentPath, currentColor, strokeWidth))
                                    currentPath = Path()
                                }
                            }
                        )
                    }
            ) {
                // Apply transformations
                scale(scale.value, scale.value) {
                    translate(offset.value.x, offset.value.y) {
                        drawGrid()
                    }
                }

                // Draw all elements
                scale(scale.value, scale.value) {
                    translate(offset.value.x, offset.value.y) {
                        elements.forEach { element ->
                            when (element.type) {
                                ElementType.PATH -> {
                                    drawPath(
                                        path = element.path.toPath(),
                                        color = element.color,
                                        style = Stroke(
                                            width = element.strokeWidth,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                                ElementType.RECTANGLE -> {
                                    val bounds = element.path.toPath().getBounds()
                                    drawRect(
                                        color = element.color,
                                        topLeft = Offset(bounds.left, bounds.top),
                                        size = androidx.compose.ui.geometry.Size(bounds.width, bounds.height),
                                        style = Stroke(width = element.strokeWidth)
                                    )
                                }
                                ElementType.OVAL -> {
                                    val bounds = element.path.toPath().getBounds()
                                    drawOval(
                                        color = element.color,
                                        topLeft = Offset(bounds.left, bounds.top),
                                        size = androidx.compose.ui.geometry.Size(bounds.width, bounds.height),
                                        style = Stroke(width = element.strokeWidth)
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                }

                // Draw current path being drawn
                if (isDrawing) {
                    scale(scale.value, scale.value) {
                        translate(offset.value.x, offset.value.y) {
                            drawPath(
                                path = currentPath,
                                color = currentColor,
                                style = Stroke(
                                    width = strokeWidth / scale.value,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }

                // Draw all animated paths
                paths.forEachIndexed { index, path ->
                    val animatedPath = animatedPaths[index] ?: return@forEachIndexed
                    
                    scale(animatedPath.scale, animatedPath.scale) {
                        translate(animatedPath.offset.x, animatedPath.offset.y) {
                            drawPath(
                                path = path.path,
                                color = path.color.copy(alpha = path.alpha),
                                style = Stroke(width = path.strokeWidth)
                            )
                        }
                    }
                }
            }

            // Toolbar
            CanvasToolbar(
                onColorSelected = { color ->
                    currentColor = color
                },
                onStrokeWidthSelected = { width ->
                    strokeWidth = width
                },
                onClear = {
                    paths.clear()
                    animatedPaths.clear()
                }
            )
        }
    }
}

// Extension to draw grid
private fun DrawScope.drawGrid() {
    val gridSpacing = 50f
    val strokeWidth = 1f
    val color = Color.Gray.copy(alpha = 0.3f)
    
    // Draw vertical lines
    for (x in 0..size.width.toInt() step gridSpacing.toInt()) {
        drawLine(
            color = color,
            start = Offset(x.toFloat(), 0f),
            end = Offset(x.toFloat(), size.height),
            strokeWidth = strokeWidth
        )
    }
    
    // Draw horizontal lines
    for (y in 0..size.height.toInt() step gridSpacing.toInt()) {
        drawLine(
            color = color,
            start = Offset(0f, y.toFloat()),
            end = Offset(size.width, y.toFloat()),
            strokeWidth = strokeWidth
        )
    }
}
