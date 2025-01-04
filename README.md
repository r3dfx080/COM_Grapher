
# COM Grapher for sound level meter
## Overview
**COM Grapher** is a simple Java-based application for reading and visualizing sound level data from a sound level meter via a COM port. The application provides:
- A real-time bar chart of the last 20 readings
- A display of the current weighted sound level (in dBA)
- Sound pressure readings have color coding (green: <=60dBA, orange: >60dBA & <90dBA, red: >=90dBA)
- The response speed (FAST or SLOW)

## Features
- **Real-time graphing**: Visualizes the last 20 sound level readings as a bar chart
- **Current reading display**: prominently shows the latest dBA value
- **Response speed indicator**: displays whether the response speed is set to FAST or SLOW (150ms and 1s respectively)
- **User-friendly interface**: simple controls with **Start** and **Stop** buttons

<figure>
    <img src="https://github.com/user-attachments/assets/21a729a3-fb3f-4037-bf84-b9f1ba3e9e00">
    <figcaption>Primary application screen. User can select and open any COM port. Bar chart scroll right to left and shows last 20 readings</figcaption>
</figure>

## Data format
| SYNC | STATUS | SPEED | INTEGER | FRACTIONAL |
|:----:|:------:|:-----:|:-------:|:----------:|
|   0  |    1   |   2   |    3    |      4     |

- Byte 0 is **always 255**. Used for synchronization. Every valid sequence starts with it
- Byte 1 - status byte, ASCII value for 'O' (OK) or 'E' (ERROR)
- Byte 2 - speed byte, ASCII value for 'F' (FAST) or 'S' (SLOW)
- Byte 3 - integer part of current reading
- Byte 4 - fractional part of current reading

