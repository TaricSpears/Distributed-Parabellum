package it.unibo.parabellum
package view

import model.function.Direction
import scalafx.scene.layout.{HBox, Priority, Region}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, Label, TextField}
import scalafx.scene.paint.Color.*
import scalafx.Includes.*
import scalafx.event.ActionEvent

/**
 * Bottom control panel providing the input area for the active player to shoot.
 * Besides the function, the player chooses in which direction along the x-axis the projectile travels.
 * @param onShoot Callback function executed when the "Shoot" button is pressed,
 *                passing the user's mathematical function input and the chosen direction.
 * @param quitAction Callback function executed when the "Quit" button is pressed.
 */
class ControlPanelView(onShoot: (String, Direction) => Unit, quitAction: () => Unit) extends HBox:

  // Create a spacer that grows horizontally to fill empty space
  val spacer = new Region:
    hgrow = Priority.Always

  padding = Insets(15)
  spacing = 20
  style = "-fx-background-color: #0b1220; -fx-border-color: #00e5ff; -fx-border-width: 1.5 0 0 0;"
  alignment = Pos.Center

  private var direction: Direction = Direction.Positive
  private var currentSoldier: String = ""

  private def arrow(d: Direction): String = d match
    case Direction.Positive => "\u2192"
    case Direction.Negative => "\u2190"

  private def flip(d: Direction): Direction = d match
    case Direction.Positive => Direction.Negative
    case Direction.Negative => Direction.Positive

  private val turnLabel = new Label("Waiting for game..."):
    textFill = White
    style = "-fx-font-size: 16px; -fx-font-weight: bold;"

  private val directionButton = new Button(s"Direction: ${arrow(Direction.Positive)}"):
    style = UiTheme.neonPanelButton
    onAction = (ae: ActionEvent) => setDirection(flip(direction))

  private val inputField = new TextField:
    promptText = "e.g. sin(x + 2) * 30"
    prefWidth = 300
    style = UiTheme.neonFieldCompact

  private val shootButton = new Button("Shoot"):
    style = UiTheme.neonPanelButton.replace("#00e5ff", "#ff2a6d").replace("rgba(10, 22, 36", "rgba(36, 10, 24")
    onAction = (ae: ActionEvent) => {
      onShoot(inputField.text.value, direction)
      // Opzionale: svuota il campo dopo aver sparato
      inputField.clear()
    }

  private val quitButton = new Button("Quit"):
    style = UiTheme.neonPanelButton
    onAction = (ae: ActionEvent) => quitAction()

  children = List(quitButton, spacer, turnLabel, directionButton, inputField, shootButton)

  private def setDirection(newDirection: Direction): Unit =
    direction = newDirection
    directionButton.text = s"Direction: ${arrow(newDirection)}"

  /**
   * Updates the UI to show which soldier is currently playing.
   * When the turn passes to another soldier, the direction goes back to the one proposed for him;
   * during his turn the player can change it as many times as he wants.
   * @param playerName The name of the active soldier.
   * @param defaultDirection The direction proposed for the active soldier.
   */
  def updateCurrentPlayer(playerName: String, defaultDirection: Direction): Unit =
    turnLabel.text = s"$playerName's turn:"
    if playerName != currentSoldier then
      currentSoldier = playerName
      setDirection(defaultDirection)
