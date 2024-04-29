(ns status-im.contexts.chat.messenger.composer.keyboard
  (:require
    [oops.core :as oops]
    [react-native.async-storage :as async-storage]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im.contexts.chat.messenger.composer.utils :as utils]))

(defn get-kb-height
  [curr-height default-height]
  (if (and default-height (< curr-height default-height))
    default-height
    curr-height))

(defn store-kb-height
  [event {:keys [kb-default-height kb-height]}]
  (let [height (- (:height (rn/get-window))
                  (oops/oget event "endCoordinates.screenY"))]
    (reset! kb-height height)
    (when (zero? @kb-default-height)
      (async-storage/set-item! :kb-default-height (str height)))))

(defn handle-emoji-kb-ios
  "Opening emoji KB on iOS will cause a flicker up and down due to height differences.
  This method handles that by adding the extra difference between the keyboards. When the input is
  expanded to a point where the added difference will make the composer go beyond the screen causing a flicker,
  we're subtracting the difference so it only reaches the allowed max-height. We're not animating these
  changes to make it appear seamless during transitions between keyboard types when maximized."
  [event
   {:keys [emoji-kb-extra-height]}
   {:keys [text-value kb-height]}
   {:keys [height saved-height]}
   {:keys [max-height]}]
  (let [start-h                 (oops/oget event "startCoordinates.height")
        end-h                   (oops/oget event "endCoordinates.height")
        diff                    (- end-h start-h)
        max-height-diff         (- max-height diff)
        curr-text               @text-value
        bigger-than-default-kb? (> end-h @kb-height)
        almost-expanded?        (> (reanimated/get-shared-value height) max-height-diff)]
    (if (and almost-expanded? bigger-than-default-kb? (pos? diff))
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
   state animations dimensions]
  (reset! keyboard-show-listener (.addListener
                                  rn/keyboard
                                  "keyboardDidShow"
                                  #(store-kb-height % state)))
  (reset! keyboard-frame-listener (.addListener
                                   rn/keyboard
                                   "keyboardWillChangeFrame"
                                   #(handle-emoji-kb-ios % props state animations dimensions)))
  (reset! keyboard-hide-listener (.addListener rn/keyboard
                                               "keyboardDidHide"
                                               #(when platform/android?
                                                  (utils/blur-input input-ref)))))

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
