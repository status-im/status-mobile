(ns status-im2.contexts.chat.composer.handlers
  (:require
    [clojure.string :as string]
    [oops.core :as oops]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [reagent.core :as reagent]
    [status-im2.contexts.chat.composer.constants :as constants]
    [status-im2.contexts.chat.composer.keyboard :as kb]
    [status-im2.contexts.chat.composer.selection :as selection]
    [status-im2.contexts.chat.composer.utils :as utils]
    [utils.debounce :as debounce]
    [utils.number]
    [utils.re-frame :as rf]))

(defn focus
  "Animate to the `saved-height`, display background-overlay if needed, and set cursor position"
  [{:keys [input-ref] :as props}
   {:keys [text-value focused? lock-selection? saved-cursor-position]}
   {:keys [height saved-height last-height opacity background-y container-opacity]
    :as   animations}
   {:keys [max-height] :as dimensions}
   show-floating-scroll-down-button?]
  (reset! focused? true)
  (rf/dispatch [:chat.ui/set-input-focused true])
  (let [last-height-value (reanimated/get-shared-value last-height)]
    (reanimated/animate height last-height-value)
    (reanimated/set-shared-value saved-height last-height-value)
    (reanimated/animate container-opacity 1)
    (when (> last-height-value (* constants/background-threshold max-height))
      (reanimated/animate opacity 1)
      (reanimated/set-shared-value background-y 0)))

  (js/setTimeout #(reset! lock-selection? false) 300)
  (when (and (not-empty @text-value) @input-ref)
    (.setNativeProps ^js @input-ref
                     (clj->js {:selection {:start @saved-cursor-position :end @saved-cursor-position}})))
  (kb/handle-refocus-emoji-kb-ios props animations dimensions)
  (reset! show-floating-scroll-down-button? false))

(defn blur
  "Save the current height, minimize the composer, animate-out the background, and save cursor position"
  [{:keys [text-value focused? lock-selection? cursor-position saved-cursor-position gradient-z-index
           maximized? recording?]}
   {:keys [height saved-height last-height gradient-opacity container-opacity opacity background-y]}
   {:keys [content-height max-height window-height]}
   {:keys [images link-previews? reply]}]
  (when-not @recording?
    (let [lines         (utils/calc-lines (- @content-height constants/extra-content-offset))
          min-height    (utils/get-min-height lines)
          reopen-height (utils/calc-reopen-height text-value
                                                  min-height
                                                  max-height
                                                  content-height
                                                  saved-height)]
      (reset! focused? false)
      (rf/dispatch [:chat.ui/set-input-focused false])
      (reanimated/set-shared-value last-height reopen-height)
      (reanimated/animate height min-height)
      (reanimated/set-shared-value saved-height min-height)
      (reanimated/animate opacity 0)
      (js/setTimeout #(reanimated/set-shared-value background-y (- window-height)) 300)
      (when (utils/empty-input? @text-value images link-previews? reply nil)
        (reanimated/animate container-opacity constants/empty-opacity))
      (reanimated/animate gradient-opacity 0)
      (reset! lock-selection? true)
      (reset! saved-cursor-position @cursor-position)
      (reset! gradient-z-index (if (= (reanimated/get-shared-value gradient-opacity) 1) -1 0))
      (when (not= reopen-height max-height)
        (reset! maximized? false)
        (rf/dispatch [:chat.ui/set-input-maximized false])))))

(defn content-size-change
  "Save new text height, expand composer if possible, show background overlay if needed"
  [event
   {:keys [maximized? lock-layout? text-value]}
   {:keys [height saved-height last-height opacity background-y]}
   {:keys [content-height window-height max-height]}
   keyboard-shown]
  (when keyboard-shown
    (let [event-size   (oops/oget event "nativeEvent.contentSize.height")
          content-size (+ event-size constants/extra-content-offset)
          lines        (utils/calc-lines event-size)
          content-size (if (or (= lines 1) (empty? @text-value))
                         constants/input-height
                         (if (= lines 2) constants/multiline-minimized-height content-size))
          new-height   (utils.number/value-in-range content-size
                                                    constants/input-height
                                                    max-height)
          new-height   (min new-height max-height)]
      (reset! content-height content-size)
      (when (utils/update-height? content-size height max-height)
        (reanimated/animate height new-height)
        (reanimated/set-shared-value last-height new-height)
        (reanimated/set-shared-value saved-height new-height))
      (when (= new-height max-height)
        (reset! maximized? true)
        (rf/dispatch [:chat.ui/set-input-maximized true]))
      (if (utils/show-background? max-height new-height maximized?)
        (do
          (reanimated/set-shared-value background-y 0)
          (reanimated/animate opacity 1))
        (when (= (reanimated/get-shared-value opacity) 1)
          (reanimated/animate opacity 0)
          (js/setTimeout #(reanimated/set-shared-value background-y (- window-height)) 300)))
      (rf/dispatch [:chat.ui/set-input-content-height content-size])
      (reset! lock-layout? (> lines 2)))))

(defn scroll
  "Hide or show top gradient while scroll"
  [event
   {:keys [scroll-y]}
   {:keys [gradient-z-index focused?]}
   {:keys [gradient-opacity]}
   {:keys [lines max-lines]}]
  (let [y (oops/oget event "nativeEvent.contentOffset.y")]
    (reset! scroll-y y)
    (when (utils/show-top-gradient? y lines max-lines gradient-opacity focused?)
      (reset! gradient-z-index 1)
      (js/setTimeout #(reanimated/animate gradient-opacity 1) 0))
    (when (utils/hide-top-gradient? y gradient-opacity)
      (reanimated/animate gradient-opacity 0)
      (js/setTimeout #(reset! gradient-z-index 0) 300))))

(defn change-text
  "Update `text-value`, update cursor selection, find links, find mentions"
  [text
   {:keys [input-ref record-reset-fn]}
   {:keys [text-value cursor-position recording?]}]
  (reset! text-value text)
  (reagent/next-tick #(when @input-ref
                        (.setNativeProps ^js @input-ref
                                         (clj->js {:selection {:start @cursor-position
                                                               :end   @cursor-position}}))))
  (when @recording?
    (@record-reset-fn)
    (reset! recording? false))
  (rf/dispatch [:chat.ui/set-chat-input-text text])
  (debounce/debounce-and-dispatch [:link-preview/unfurl-urls text]
                                  constants/unfurl-debounce-ms)
  (if (string/ends-with? text "@")
    (rf/dispatch [:mention/on-change-text text])
    (debounce/debounce-and-dispatch [:mention/on-change-text text] 300)))

(defn selection-change
  "A method that handles our custom selector for `B I U`"
  [event
   {:keys [input-ref selection-event selection-manager]}
   {:keys [lock-selection? cursor-position first-level? menu-items]}]
  (let [start             (oops/oget event "nativeEvent.selection.start")
        end               (oops/oget event "nativeEvent.selection.end")
        selection?        (not= start end)
        text-input-handle (rn/find-node-handle @input-ref)]
    (when-not @lock-selection?
      (reset! cursor-position end))
    (when (and selection? (not @first-level?))
      (js/setTimeout #(oops/ocall selection-manager :startActionMode text-input-handle) 500))
    (when (and (not selection?) (not @first-level?))
      (oops/ocall selection-manager :hideLastActionMode)
      (selection/reset-to-first-level-menu first-level? menu-items))
    (when @selection-event
      (let [{:keys [start end text-input-handle]} @selection-event]
        (selection/update-selection text-input-handle start end)
        (reset! selection-event nil)))))

(defn layout
  [event state blur-height]
  (when (utils/update-blur-height? event state blur-height)
    (reanimated/set-shared-value blur-height (oops/oget event "nativeEvent.layout.height"))))
