(ns status-im2.contexts.chat.composer.gesture
  (:require
    [react-native.gesture :as gesture]
    [react-native.reanimated :as reanimated]
    [oops.core :as oops]
    [status-im2.contexts.chat.composer.constants :as constants]
    [status-im2.contexts.chat.composer.utils :as utils]
    [utils.re-frame :as rf]))

(defn set-opacity
  [velocity opacity translation expanding? min-height max-height new-height saved-height]
  (let [remaining-height     (if expanding?
                               (- max-height (reanimated/get-shared-value saved-height))
                               (- (reanimated/get-shared-value saved-height) min-height))
        progress             (if (= new-height min-height) 1 (/ translation remaining-height))
        currently-expanding? (neg? velocity)
        max-opacity?         (and currently-expanding? (= (reanimated/get-shared-value opacity) 1))
        min-opacity?         (and (not currently-expanding?)
                                  (= (reanimated/get-shared-value opacity) 0))]
    (if (>= translation 0)
      (when (and (not expanding?) (not min-opacity?))
        (reanimated/set-shared-value opacity (- 1 progress)))
      (when (and expanding? (not max-opacity?))
        (reanimated/set-shared-value opacity (Math/abs progress))))))

(defn maximize
  [{:keys [maximized?]}
   {:keys [height saved-height background-y opacity]}
   {:keys [max-height]}]
  (reanimated/animate height max-height)
  (reanimated/set-shared-value saved-height max-height)
  (reanimated/set-shared-value background-y 0)
  (reanimated/animate opacity 1)
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
  (when @input-ref
    (.blur ^js @input-ref)))

(defn bounce-back
  [{:keys [height saved-height opacity background-y]}
   {:keys [window-height]}
   starting-opacity]
  (reanimated/animate height (reanimated/get-shared-value saved-height))
  (when (zero? starting-opacity)
    (reanimated/animate opacity 0)
    (reanimated/animate-delay background-y (- window-height) 300)))

(defn drag-gesture
  [{:keys [input-ref] :as props}
   {:keys [gesture-enabled? text-value] :as state}
   {:keys [height saved-height last-height opacity background-y container-opacity] :as animations}
   {:keys [max-height lines] :as dimensions}
   keyboard-shown]
  (let [expanding?       (atom true)
        starting-opacity (reanimated/get-shared-value opacity)]
    (-> (gesture/gesture-pan)
        (gesture/enabled @gesture-enabled?)
        (gesture/on-start (fn [event]
                            (if-not keyboard-shown
                              (do ; focus and end
                                (reset! text-value (str (oops/oget event "velocityY")))
                                (when (< (oops/oget event "velocityY") constants/velocity-threshold)
                                  (reanimated/set-shared-value container-opacity 1)
                                  (reanimated/set-shared-value last-height max-height))
                                (when @input-ref
                                  (.focus ^js @input-ref))
                                (reset! gesture-enabled? false))
                              (do ; else, will start gesture
                                (reanimated/set-shared-value background-y 0)
                                (reset! expanding? (neg? (oops/oget event "velocityY")))))))
        (gesture/on-update
         (fn [event]
           (let [translation    (oops/oget event "translationY")
                 min-height     (utils/get-min-height lines)
                 new-height     (- (reanimated/get-shared-value saved-height) translation)
                 bounded-height (utils/bounded-val new-height min-height max-height)]
             (when keyboard-shown
               (if (>= new-height min-height)
                 (do ; expand sheet
                   (reanimated/set-shared-value height bounded-height)
                   (set-opacity (oops/oget event "velocityY")
                                opacity
                                translation
                                @expanding?
                                min-height
                                max-height
                                bounded-height
                                saved-height))
                 (when @input-ref ; sheet at min-height, collapse keyboard
                   (.blur ^js @input-ref)))))))
        (gesture/on-end (fn []
                          (let [diff (- (reanimated/get-shared-value height)
                                        (reanimated/get-shared-value saved-height))]
                            (if @gesture-enabled?
                              (if (>= diff 0)
                                (if (> diff constants/drag-threshold)
                                  (maximize state animations dimensions)
                                  (bounce-back animations dimensions starting-opacity))
                                (if (> (Math/abs diff) constants/drag-threshold)
                                  (minimize props state)
                                  (bounce-back animations dimensions starting-opacity)))
                              (reset! gesture-enabled? true))))))))


