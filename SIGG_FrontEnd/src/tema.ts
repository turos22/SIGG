import { createTheme } from '@mui/material/styles'

export const tema = createTheme({
  palette: {
    primary: { main: '#0f4c8a' },
    success: { main: '#2c8a57' },
    error: { main: '#a8321f' },
    text: { primary: '#10263f', secondary: '#55555a' },
  },
  shape: { borderRadius: 10 },
  typography: {
    fontFamily: "system-ui, 'Segoe UI', Roboto, sans-serif",
    button: { textTransform: 'none', fontWeight: 600 },
  },
})
