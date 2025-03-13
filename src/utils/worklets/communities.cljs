(ns utils.worklets.communities)

(def worklets (js/require "../src/js/worklets/communities.js"))

(def use-logo-styles (.-useLogoStyles worklets))
(def use-sheet-styles (.-useSheetStyles worklets))
(def use-name-styles (.-useNameStyles worklets))
(def use-info-styles (.-useInfoStyles worklets))
(def use-channels-styles (.-useChannelsStyles worklets))
(def use-scroll-to (.-useScrollTo worklets))
(def use-header-opacity (.-useHeaderOpacity worklets))
(def use-opposite-header-opacity (.-useOppositeHeaderOpacity worklets))
(def use-nav-content-opacity (.-useNavContentOpacity worklets))

(def on-pan-start (.-onPanStart worklets))
(def on-pan-update (.-onPanUpdate worklets))
(def on-pan-end (.-onPanEnd worklets))
