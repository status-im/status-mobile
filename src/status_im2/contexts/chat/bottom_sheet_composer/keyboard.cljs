(ns status-im2.contexts.chat.bottom-sheet-composer.keyboard
  (:require [oops.core :as oops]
            [status-im.async-storage.core :as async-storage]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]))

(defn get-kb-height
  [curr-height default-height]
  (if (and default-height (< curr-height default-height))
    default-height
    curr-height))

(defn store-kb-height
  [{:keys [kb-default-height]} keyboard-height]
  (when (and (not @kb-default-height) (pos? keyboard-height))
    (async-storage/set-item! :kb-default-height (str keyboard-height))))

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

(defn add-kb-listeners
  [{:keys [keyboard-show-listener keyboard-frame-listener keyboard-hide-listener input-ref] :as props}
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
                                               #(when (and platform/android? @input-ref)
                                                  (.blur ^js @input-ref)))))

(defn handle-refocus-emoji-kb-ios
  [{:keys [saved-emoji-kb-extra-height]}
   {:keys [height saved-height last-height]}
   {:keys [lines max-lines]}]
  (when @saved-emoji-kb-extra-height
    (js/setTimeout (fn []
                     (when (> lines max-lines)
                       (reanimated/animate height
                                           (+ (reanimated/get-shared-value last-height)
                                              @saved-emoji-kb-extra-height))
                       (reanimated/set-shared-value saved-height
                                                    (+ (reanimated/get-shared-value last-height)
                                                       @saved-emoji-kb-extra-height)))
                     (reset! saved-emoji-kb-extra-height nil))
                   600)))
