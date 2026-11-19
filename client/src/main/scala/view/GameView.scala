package it.unibo.parabellum
package view

import scalafx.animation.PauseTransition
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.layout.{HBox, Pane, Priority, VBox}
import scalafx.scene.Node
import scalafx.scene.control.{Button, Label, ScrollPane, TextField}
import scalafx.scene.control.ScrollPane.ScrollBarPolicy
import scalafx.scene.paint.Color.{Black, White}
import scalafx.scene.shape.Line
import scalafx.util.Duration

/**
 * Contenitore principale per tutti gli elementi grafici del livello di gioco.
 * Interfaccia tra la logica di gioco e la visualizzazione a schermo.
 */
class GameView(width: Double, height: Double) extends Pane:
  style = "-fx-background-color: darkgray;"
  // 1. Forza le dimensioni preferite di questo pannello
  prefWidth = width
  prefHeight = height

  private val errorTimer = new PauseTransition(Duration(6000))
  errorTimer.onFinished = _ => {
    errorLabel.visible = false
    errorLabel.text = ""
  }

  // 2. Crea gli assi cartesiani come campi privati
  private val xAxis = new Line:
    startX = 0
    startY = height / 2
    endX = width
    endY = height / 2
    stroke = Black
    strokeWidth = 1.0
    opacity = 0.5

  private val yAxis = new Line:
    startX = width / 2
    startY = 0
    endX = width / 2
    endY = height
    stroke = Black
    strokeWidth = 1.0
    opacity = 0.5

  private val errorLabel: Label = new Label:
    textFill = White
    style = """
    -fx-background-color: rgba(0, 0, 0, 0.7);
    -fx-font-family: 'monospace';
    -fx-text-alignment: left;
    -fx-font-weight: bold;
    -fx-padding: 10px;
    -fx-background-radius: 8px;
    """
    visible = false

  // ---------------------------------------------------------------- in-game chat overlay (online matches)

  private var onSendChat: String => Unit = _ => ()
  private var chatExpanded = false
  private var unreadCount = 0

  private val chatMessagesBox = new VBox:
    spacing = 4
    padding = Insets(4)

  private val chatScroll = new ScrollPane:
    content = chatMessagesBox
    fitToWidth = true
    prefHeight = 135
    maxHeight = 135
    hbarPolicy = ScrollBarPolicy.Never
    style = "-fx-background: transparent; -fx-background-color: transparent;"

  private val chatInput = new TextField:
    promptText = "Write a message..."
    hgrow = Priority.Always
    onAction = _ => sendCurrentChat()

  private val chatSendButton = new Button("Send"):
    style = "-fx-font-size: 11pt; -fx-font-weight: bold; -fx-cursor: hand;"
    onAction = _ => sendCurrentChat()

  private val chatInputRow = new HBox:
    spacing = 4
    alignment = Pos.CenterLeft
    children = List[Node](chatInput, chatSendButton)

  private val chatBody = new VBox:
    spacing = 6
    padding = Insets(8)
    prefWidth = 270
    style = "-fx-background-color: rgba(0, 0, 0, 0.72); -fx-background-radius: 8;"
    visible = false
    managed = false
    children = List[Node](chatScroll, chatInputRow)

  private val chatToggleButton = new Button("💬 Chat"):
    style = "-fx-background-color: rgba(0, 0, 0, 0.72); -fx-text-fill: white; -fx-font-size: 11pt; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 5 12 5 12; -fx-cursor: hand;"
    onAction = _ => toggleChat()

  private val chatOverlay = new VBox:
    layoutX = 12
    layoutY = 12
    spacing = 4
    visible = false
    managed = false
    children = List[Node](chatToggleButton, chatBody)

  private def updateToggleLabel(): Unit =
    if !chatExpanded && unreadCount > 0 then
      chatToggleButton.text = s"💬 Chat ($unreadCount)"
      chatToggleButton.style = "-fx-background-color: #f0c040; -fx-text-fill: black; -fx-font-size: 11pt; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 5 12 5 12; -fx-cursor: hand;"
    else
      chatToggleButton.text = if chatExpanded then "💬 Hide chat" else "💬 Chat"
      chatToggleButton.style = "-fx-background-color: rgba(0, 0, 0, 0.72); -fx-text-fill: white; -fx-font-size: 11pt; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 5 12 5 12; -fx-cursor: hand;"

  private def toggleChat(): Unit =
    chatExpanded = !chatExpanded
    if chatExpanded then unreadCount = 0
    chatBody.visible = chatExpanded
    chatBody.managed = chatExpanded
    updateToggleLabel()
    chatOverlay.toFront()

  private def sendCurrentChat(): Unit =
    val text = chatInput.text.value.trim
    if text.nonEmpty then
      chatInput.clear()
      onSendChat(text)

  /** Enables or hides the in-game chat overlay (shown only for online matches). */
  def setChatEnabled(enabled: Boolean, onSend: String => Unit): Unit =
    onSendChat = onSend
    chatMessagesBox.children.clear()
    chatInput.clear()
    chatExpanded = false
    unreadCount = 0
    chatBody.visible = false
    chatBody.managed = false
    chatOverlay.visible = enabled
    chatOverlay.managed = enabled
    updateToggleLabel()
    if enabled then chatOverlay.toFront()

  /** Appends a message to the in-game chat box. */
  def addChatMessage(sender: String, text: String): Unit =
    val line = new Label(s"$sender: $text"):
      style = "-fx-text-fill: white; -fx-font-size: 11pt;"
      wrapText = true
      maxWidth = 240
    chatMessagesBox.children.add(line.delegate)
    chatScroll.vvalue = 1.0
    if !chatExpanded then
      unreadCount += 1
      updateToggleLabel()
    chatOverlay.toFront()

  children.addAll(xAxis, yAxis, errorLabel, chatOverlay)

  /**
   * Aggiunge uno o più elementi grafici al livello.
   *
   * @param nodes Elementi grafici da aggiungere (Player, Obstacle, Projectile, ecc.)
   */
  def addElements(nodes: Node*): Unit =
    children.addAll(nodes.map(_.delegate))
    chatOverlay.toFront()

  /**
   * Rimuove un elemento grafico specifico dal livello.
   *
   * @param node L'elemento grafico da rimuovere
   */
  def removeElement(node: Node): Unit =
    children.remove(node.delegate)

  /**
   * Pulisce l'intera scena rimuovendo tutti gli elementi.
   */
  def clear(): Unit =
    children.clear()
    children.addAll(xAxis, yAxis, errorLabel, chatOverlay)

  def showTemporaryError(message: String): Unit = {
    errorLabel.text = message
    errorLabel.visible = true
    errorLabel.toFront()

    errorTimer.playFromStart()
  }
