# RetroByte-Blitz
Brain training game inspired by the old Brain Champion Nokia game.

## Features
- Four surprise **_daily exercises_**, one from each category, to compete against friends
- **_Leaderboard_** and statistics line graph with the **_progress per brain area_**
- **_Calendar_** to track the finishing of daily exercises
- 12 games across **_4 categories_** to play at leisure
- Account with own avatar picture and stored progress
## Walkthrough
**Register** and **login** with a valid account.

<img src="assets/login.png" alt="Login Screen" width="300"/>  <img src="assets/main_menu.png" alt="Main Menu" width="300"/>

From the _main menu_ choose either the **daily exercises** option or any of the **categories**. There are short instructions before each game.

**Daily exercises:**

<img src="assets/daily_exercises.png" alt="Daily exercises screen with 4 games: Number of, Colors, Moving Sum, Roman Numerals" width="300"/> <img src="assets/instructions.png" alt="Instructions" width="300"/>

Leaving mid-game does not save the score or pause the mini game.

The score of daily exercises counts towards the _leaderboard_ for the day.

<img src="assets/final_score.png" alt="Final Score Popup" width="300"/> <img src="assets/leaderboard.png" alt="Leaderboard" width="300"/>

Press the **chart** icon in the bottom navigation menu to access the _statistics_. A graph with the progress per category covers the last 10 fully completed days. The calendar showcases how many daily games out of 4 were completed on a certain day.

<img src="assets/statistics.png" alt="Progress graph and calendat" width="300"/>

Press the **gear** icon in the bottom navigation menu to open the Settings drawer. From here change the _username, profile picture or log out_ . Detailed instructions for each game are below.

<img src="assets/change_avatar.png" alt="Profile picture options" width="300"/> <img src="assets/change_username.png" alt="Username changed sucessfully" width="300"/>

---
### Logic category:

- **Number of:** Count the number of appearances of a certain character
  
  <img src="assets/number_of.png" alt="Number of Game" width="300"/>
  
- **Sudoku:** Classic sudoku game

  <img src="assets/sudoku.png" alt="Sudoku Game" width="300"/>
  
- **Slider:** Complete the puzzle by switching pieces 2 at a time
    - Take you own picture or use the default one.
      
  <img src="assets/slider_choice.png" alt="Popup to take picture or use default" width="300"/> <img src="assets/slider.png" alt="Slider Game" width="300"/>

### Memory category:
- **Colors:** Remember and recreate the shown color sequence

  <img src="assets/colors.png" alt="Colors Game" width="300"/>
  
- **Grid:** Remember which squares light up and click to turn them on

  <img src="assets/grid.png" alt="Grid Game" width="300"/>
  
- **Card:** Match pairs of identical objects
  
  <img src="assets/card.png" alt="Card Game" width="300"/>
  
### Calculation category:
- **Calculation:** Perform arithmetic operations and enter the result

  <img src="assets/calculation.png" alt="Calculation Game" width="300"/>
  
- **Sequence:** Complete the arithmetic or geometric sequence with the next term

  <img src="assets/sequence.png" alt="Sequence Game" width="300"/>
  
- **Moving Sum:** Enter the sum of the moving numbers

  <img src="assets/moving_sum.png" alt="Moving Sum Game" width="300"/>

### Visual category:
- **Descending:** Count the characters and place them in descending order of number of occurances

  <img src="assets/descending.png" alt="Descending Game" width="300"/>
  
- **Stroop:** Identify the color a word is displayed in, ignoring the meaning (the word “Blue” printed in red ink should be answered as “Red”)

  <img src="assets/stroop.png" alt="Stroop Game" width="300"/>
  
- **Roman numerals:** Match pairs of Arab and Roman numberals

  <img src="assets/roman_numerals.png" alt="Roman Numerals Game" width="300"/>

## Installation
You can run the app locally via Android Studio or install the APK manually from the [Releases](../../releases) page.

### Option 1: Run via Android Studio

1. **Clone the repository**

   ```bash
   git clone https://github.com/your-username/brain-training-game.git
   ```

2. **Open the project in Android Studio**

3. **Build the project**

     Android Studio will prompt you to sync Gradle and download dependencies.

4. **Run the app on a device or emulator**

   *  **To use an Android device**:
      * Connect it via USB and enable **Developer Options** > **USB Debugging**
      * Your device should appear under "Available Devices" in the Run menu
   *  **To use an emulator**:

      * Go to **Tools > Device Manager** in Android Studio
      * Click **Create Device**, choose a phone model (e.g., Medium Phone), and select a system image (e.g., API 30)
      * Start the emulator and run the app

### Option 2: Install the APK

1. Visit the [Releases](../../releases) section
2. Download the latest `.apk` file
3. Transfer the APK to your Android device
4. Open it and follow prompts to install
   *(Allow "Install from unknown sources" if prompted)*

