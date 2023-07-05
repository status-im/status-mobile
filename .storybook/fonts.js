import inter_bold from '../resources/fonts/Inter-Bold.otf';
import inter_bold_italic from '../resources/fonts/Inter-BoldItalic.otf';
import inter_italic from '../resources/fonts/Inter-Italic.otf';
import inter_medium from '../resources/fonts/Inter-Medium.otf';
import inter_medium_italic from '../resources/fonts/Inter-MediumItalic.otf';
import inter_regular from '../resources/fonts/Inter-Regular.otf';
import inter_semi_bold from '../resources/fonts/Inter-SemiBold.otf';
import inter_semi_bold_italic from '../resources/fonts/Inter-SemiBoldItalic.otf';
import inter_status_regular from '../resources/fonts/InterStatus-Regular.otf';
import ubuntu_mono_regular from '../resources/fonts/UbuntuMono-Regular.ttf';

const fonts = {
  'Inter-Bold': inter_bold,
  'Inter-BoldItalic': inter_bold_italic,
  'Inter-Italic': inter_italic,
  'Inter-Medium': inter_medium,
  'Inter-MediumItalic': inter_medium_italic,
  'Inter-Regular': inter_regular,
  'Inter-SemiBold': inter_semi_bold,
  'Inter-SemiBoldItalic': inter_semi_bold_italic,
  'InterStatus-Regular': inter_status_regular,
  'UbuntuMono-Regular': ubuntu_mono_regular,
};

const fontStyles = Object.entries(fonts).reduce((prev, [fontName, font]) => {
  return `${prev}
  @font-face {
    src: url(${font});
    font-family: ${fontName};
  }`;
}, '');

const style = document.createElement('style');
style.type = 'text/css';
if (style.styleSheet) {
  style.styleSheet.cssText = fontStyles;
} else {
  style.appendChild(document.createTextNode(fontStyles));
}

// Inject stylesheet
document.head.appendChild(style);
