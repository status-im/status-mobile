//import {useAnimatedStyle, useDerivedValue} from 'react-native-reanimated';

const babel = require('@babel/core');
const plugin = require('./node_modules/react-native-reanimated/plugin');

// const codeString = "goog.provide(\x27status_im.contexts.profile.settings.header.avatar\x27);\nstatus_im.contexts.profile.settings.header.avatar.f_avatar \x3d (function status_im$contexts$profile$settings$header$avatar$f_avatar(a){\nvar sv \x3d react_native.reanimated.use_shared_value.call(null,(0));\nvar THIS_THING \x3d shadow.js.shim.module$react_native_reanimated.useAnimatedStyle((function status_im$contexts$profile$settings$header$avatar$f_avatar_$_HEEEEEEY(){\n\x27worklet;\x27;\n\nconsole.log(\x22hola\x22);\n\nreturn ({\x22transform\x22: [({\x22translateX\x22: sv.value})]});\n}));\nreturn null;\n});\nstatus_im.contexts.profile.settings.header.avatar.view \x3d (function status_im$contexts$profile$settings$header$avatar$view(props){\nreturn new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,\x22f\x3e\x22,\x22f\x3e\x22,1484564198),status_im.contexts.profile.settings.header.avatar.f_avatar,props], null);\n});\n"
//
// const fs = require('fs');
//
// function workletize(s) {
//     return babel.transformSync(eval(s), {
//         filename: '/dev/null', compact: false, plugins: [plugin]
//     }).code
// }
//
// function extractStrings(filePath) {
//     try {
//         // Read the content of the file
//         const fileContent = fs.readFileSync(filePath, 'utf8');
//
//         // Use a regular expression to find all strings enclosed in double quotes
//         const stringMatches = fileContent.match(
//             /(?<!\\)"(goog\.provide\(.+?)"/g
//         );
//
//         // transforms the code
//         const mod = stringMatches.map(workletize);
//
//         // Return the array of matched strings
//         return mod.filter(str => str !== "") || [];
//     } catch (err) {
//         console.error('Error reading the file:', err.message);
//         return [];
//     }
// }
//
// const filePath = './result/index.js';
// const result = extractStrings(filePath);
//
//
// console.log('Strings found in the file:');
// result.forEach((str, index) => {
//     console.log(`${index + 1}: ${str}`);
// });
//
// // console.log(codeString);
//
// // transformedCode = babel.transformSync(codeString, {
// //     filename: '/dev/null',
// //     compact: false,
// //     plugins: [plugin]
// // }).code
// //
// // console.log(transformedCode);
//
// // SHADOW_ENV.evalLoad


const fs = require('fs');

function transformString(inputString) {
    // Customize this function based on your transformation logic
    return babel.transformSync(eval(inputString), {
        filename: '/dev/null', compact: false, plugins: [plugin]
    }).code
}

function processFile(filePath) {
    try {
        // Read the content of the file
        let fileContent = fs.readFileSync(filePath, 'utf8');

        // Use a regular expression to find strings in the file
        const stringMatches = fileContent.match(
            /(?<!\\)"(goog\.provide\(.+?)"/g
        );

        // Apply the transformation function to each matched string
        if (stringMatches) {
            stringMatches.forEach(match => {
                if (match.indexOf("HEEEEEEY") !== -1){
                    const transformedString = transformString(match);
                    fileContent = fileContent.replace(match, JSON.stringify(transformedString));
                }
            });

            // Write the modified content back to the file
            fs.writeFileSync(filePath, fileContent, 'utf8');

            console.log('File successfully processed.');
        } else {
            console.log('No strings found in the file.');
        }
    } catch (err) {
        console.error('Error processing the file:', err.message);
    }
}

// Example usage
const filePath = "./result/index.js";
processFile(filePath);
