(ns status-im.contexts.chat.messenger.composer.gesture
  (:require
    [oops.core :as oops]
    [react-native.gesture :as gesture]
    [react-native.reanimated :as reanimated]
    [status-im.contexts.chat.messenger.composer.constants :as constants]
    [status-im.contexts.chat.messenger.composer.utils :as utils]
    [utils.number]
    [utils.re-frame :as rf]))

(defn maximize
  [{:keys [maximized?]}
   {:keys [height saved-height]}
   {:keys [max-height]}]
  (reanimated/animate height max-height)
  (reanimated/set-shared-value saved-height max-height)
  (reset! maximized? true)
  (rf/dispatch [:chat.ui/set-input-maximized true]))

(defn minimize
  [{:keys [input-ref emoji-kb-extra-height saved-emoji-kb-extra-height]}
   {:keys [maximized?]}]
  (when @emoji-kb-extra-height
    (reset! saved-emoji-kb-extra-height @emoji-kb-extra-height)
    (reset! emoji-kb-extra-height nil))
  (reset! maximized? false)
  (rf/dispatch [:chat.ui/set-input-maximized false])
  (utils/blur-input input-ref))

(defn bounce-back
  [{:keys [height saved-height]}
   {:keys [window-height]}]
  (reanimated/animate height (reanimated/get-shared-value saved-height)))

(defn drag-gesture
  [{:keys [input-ref] :as props}
   {:keys [gesture-enabled?] :as state}
   {:keys [height saved-height last-height] :as animations}
   {:keys [max-height lines] :as dimensions}
   keyboard-shown]
  (let [expanding? (atom true)]
    (-> (gesture/gesture-pan)
        (gesture/enabled @gesture-enabled?)
        (gesture/on-start (fn [event]
                            (if-not keyboard-shown
                              (do ; focus and end
                                (when (< (oops/oget event "velocityY") constants/velocity-threshold)
                                  (reanimated/set-shared-value last-height max-height)
                                  (maximize state animations dimensions))
                                (when @input-ref
                                  (.focus ^js @input-ref))
                                (reset! gesture-enabled? false))
                              ; else, will start gesture
                              (reset! expanding? (neg? (oops/oget event "velocityY"))))))
        (gesture/on-update
         (fn [event]
           (let [translation    (oops/oget event "translationY")
                 min-height     (utils/get-min-height lines)
                 new-height     (- (reanimated/get-shared-value saved-height) translation)
                 bounded-height (utils.number/value-in-range new-height min-height max-height)]
             (when keyboard-shown
               (if (>= new-height min-height)
                 (reanimated/set-shared-value height bounded-height)
                 ; sheet at min-height, collapse keyboard
                 (utils/blur-input input-ref))))))
        (gesture/on-end (fn []
                          (let [diff (- (reanimated/get-shared-value height)
                                        (reanimated/get-shared-value saved-height))]
                            (if @gesture-enabled?
                              (if (and @expanding? (>= diff 0))
                                (if (> diff constants/drag-threshold)
                                  (maximize state animations dimensions)
                                  (bounce-back animations dimensions))
                                (if (> (Math/abs diff) constants/drag-threshold)
                                  (minimize props state)
                                  (bounce-back animations dimensions)))
                              (reset! gesture-enabled? true))))))))
