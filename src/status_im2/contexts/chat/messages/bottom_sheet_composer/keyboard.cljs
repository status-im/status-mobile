(ns status-im2.contexts.chat.messages.bottom-sheet-composer.keyboard
  (:require [oops.core :as oops]
            [react-native.async-storage :as async-storage]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.chat.messages.bottom-sheet-composer.utils :as utils]))

(defn get-kb-height
  [curr-height default-height]
  (if (and default-height (< curr-height default-height))
    default-height
    curr-height))

(defn store-kb-height
  [{:keys [kb-default-height]} keyboard-height]
  (when (and (not @kb-default-height) (pos? keyboard-height))
    (async-storage/set-item :kb-default-height keyboard-height)))

(defn handle-emoji-kb-ios
  [e
   {:keys [emoji-kb-extra-height]}
   {:keys [text-value]}
   {:keys [height saved-height]}
   {:keys [max-height]}]
  (let [start-h   (oops/oget e "startCoordinates.height")
        end-h     (oops/oget e "endCoordinates.height")
        diff      (- end-h start-h)
        max       (- max-height diff)
        curr-text @text-value]
    (if (> (reanimated/get-shared-value height) max)
      (do
        (reanimated/set-shared-value height (- (reanimated/get-shared-value height) diff))
        (reanimated/set-shared-value saved-height (- (reanimated/get-shared-value saved-height) diff))
        (reset! text-value (str @text-value " "))
        (js/setTimeout #(reset! text-value curr-text) 0)
        (reset! emoji-kb-extra-height diff))
      (when @emoji-kb-extra-height
        (reanimated/set-shared-value height
                                     (+ (reanimated/get-shared-value height) @emoji-kb-extra-height))
        (reanimated/set-shared-value saved-height
                                     (+ (reanimated/get-shared-value saved-height)
                                        @emoji-kb-extra-height))
        (reset! emoji-kb-extra-height nil)))))

(defn handle-kb-hide-android
  [{:keys [input-ref]}
   {:keys [opacity height saved-height background-y]}
   {:keys [window-height lines]}]
  (when platform/android?
    (let [min-height (utils/get-min-height lines)]
      (.blur ^js @input-ref)
      (reanimated/animate opacity 0)
      (js/setTimeout (fn []
                       (reanimated/animate height min-height)
                       (reanimated/set-shared-value saved-height min-height)
                       (reanimated/set-shared-value background-y (- window-height)))
                     100))))

(defn add-kb-listeners
  [{:keys [keyboard-show-listener keyboard-frame-listener keyboard-hide-listener] :as props}
   state animations dimensions keyboard-height]
  (reset! keyboard-show-listener (.addListener rn/keyboard
                                               "keyboardDidShow"
                                               #(store-kb-height state keyboard-height)))
  (reset! keyboard-frame-listener (.addListener
                                   rn/keyboard
                                   "keyboardWillChangeFrame"
                                   #(handle-emoji-kb-ios % props state animations dimensions)))
  (reset! keyboard-hide-listener (.addListener rn/keyboard
                                               "keyboardDidHide"
                                               #(handle-kb-hide-android props animations dimensions))))
