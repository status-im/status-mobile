(ns status-im2.contexts.chat.bottom-sheet-composer.handlers
  (:require [react-native.reanimated :as reanimated]
            [reagent.core :as reagent]
            [oops.core :as oops]
            [status-im2.contexts.chat.bottom-sheet-composer.constants :as c]
            [status-im2.contexts.chat.bottom-sheet-composer.keyboard :as kb]
            [status-im2.contexts.chat.bottom-sheet-composer.utils :as utils]
            [utils.re-frame :as rf]))

(defn focus
  [{:keys [input-ref] :as props}
   {:keys [text-value focused? lock-selection? saved-cursor-position gradient-z-index]}
   {:keys [height saved-height last-height opacity background-y gradient-opacity container-opacity]
    :as   animations}
   {:keys [max-height] :as dimensions}]
  (reset! focused? true)
  (reanimated/animate height (reanimated/get-shared-value last-height))
  (reanimated/set-shared-value saved-height (reanimated/get-shared-value last-height))
  (reanimated/animate container-opacity 1)
  (when (> (reanimated/get-shared-value last-height) (* c/background-threshold max-height))
    (reanimated/animate opacity 1)
    (reanimated/set-shared-value background-y 0))
  (when (= @gradient-z-index -1)
    (reanimated/animate gradient-opacity 1)
    (reset! gradient-z-index 1))
  (js/setTimeout #(reset! lock-selection? false) 300)
  (when (not-empty @text-value)
    (.setNativeProps ^js @input-ref
                     (clj->js {:selection {:start @saved-cursor-position :end @saved-cursor-position}})))
  (kb/handle-refocus-emoji-kb-ios props animations dimensions))


(defn blur
  [{:keys [text-value focused? lock-selection? cursor-position saved-cursor-position gradient-z-index
           maximized?]}
   {:keys [height saved-height last-height gradient-opacity container-opacity opacity background-y]}
   {:keys [lines content-height max-height window-height]}
   images]
  (let [min-height    (utils/get-min-height lines)
        reopen-height (utils/calc-reopen-height text-value min-height content-height saved-height)]
    (reset! focused? false)
    (reanimated/set-shared-value last-height reopen-height)
    (reanimated/animate height min-height)
    (reanimated/set-shared-value saved-height min-height)
    (reanimated/animate opacity 0)
    (js/setTimeout #(reanimated/set-shared-value background-y (- window-height)) 300)
    (when (and (empty? @text-value) (not (seq images)))
      (reanimated/animate container-opacity 0.7))
    (reanimated/animate gradient-opacity 0)
    (reset! lock-selection? true)
    (reset! saved-cursor-position @cursor-position)
    (reset! gradient-z-index (if (= (reanimated/get-shared-value gradient-opacity) 1) -1 0))
    (when (not= reopen-height max-height)
      (reset! maximized? false))))

(defn content-size-change
  [e
   {:keys [maximized?]}
   {:keys [height saved-height opacity background-y]}
   {:keys [content-height window-height max-height]}]
  (let [content-size (+ (oops/oget e "nativeEvent.contentSize.height") c/extra-content-offset)
        new-height   (utils/bounded-val content-size c/input-height max-height)]
    (reset! content-height content-size)
    (when (utils/should-update-height content-size height max-height)
      (reanimated/animate height new-height)
      (reanimated/set-shared-value saved-height new-height))
    (when (= new-height max-height)
      (reset! maximized? true))
    (if (or (> new-height (* c/background-threshold max-height))
            (= (reanimated/get-shared-value saved-height) max-height))
      (do
        (reanimated/set-shared-value background-y 0)
        (reanimated/animate opacity 1))
      (when (= (reanimated/get-shared-value opacity) 1)
        (reanimated/animate opacity 0)
        (js/setTimeout #(reanimated/set-shared-value background-y (- window-height)) 300)))
    (rf/dispatch [:chat.ui/set-input-content-height new-height])))

(defn scroll
  [e
   {:keys [gradient-z-index focused?]}
   {:keys [gradient-opacity]}
   {:keys [lines max-lines]}]
  (let [y (oops/oget e "nativeEvent.contentOffset.y")]
    (when (utils/should-show-top-gradient y lines max-lines gradient-opacity focused?)
      (reset! gradient-z-index 1)
      (js/setTimeout #(reanimated/animate gradient-opacity 1) 0))
    (when (utils/should-hide-top-gradient y gradient-opacity)
      (reanimated/animate gradient-opacity 0)
      (js/setTimeout #(reset! gradient-z-index 0) 300))))

(defn change-text
  [text
   {:keys [input-ref]}
   {:keys [text-value cursor-position]}]
  (reset! text-value text)
  (reagent/next-tick #(.setNativeProps ^js @input-ref
                                       (clj->js {:selection {:start @cursor-position
                                                             :end   @cursor-position}})))
  (rf/dispatch [:chat.ui/set-chat-input-text text]))

(defn selection-change
  [e {:keys [lock-selection? cursor-position]}]
  (when-not @lock-selection?
    (reset! cursor-position (oops/oget e "nativeEvent.selection.end"))))

(defn layout
  [e
   {:keys [lock-layout?]}
   blur-height]
  (when (utils/should-update-blur-height e lock-layout? blur-height)
    (reanimated/set-shared-value blur-height (oops/oget e "nativeEvent.layout.height"))))
