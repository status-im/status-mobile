(ns legacy.status-im.ui.screens.keycard.pin.views
  (:require-macros [legacy.status-im.utils.views :refer [defview letsubs]])
  (:require
    [legacy.status-im.ui.components.animation :as animation]
    [legacy.status-im.ui.components.checkbox.view :as checkbox]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.screens.keycard.pin.styles :as styles]
    [re-frame.core :as re-frame]
    [react-native.platform :as platform]
    [reagent.core :as reagent]
    [utils.i18n :as i18n]))

(def default-pin-retries-number 3)
(def default-puk-retries-number 5)

(defn numpad-button
  [n step enabled? small-screen?]
  [react/touchable-highlight
   {:on-press            #(when enabled?
                            (re-frame/dispatch [:keycard.ui/pin-numpad-button-pressed n step]))
    :accessibility-label (str "numpad-button-" n)}
   [react/view (styles/numpad-button small-screen?)
    [react/text {:style styles/numpad-button-text}
     n]]])

(defn numpad-row
  [[a b c] step enabled? small-screen?]
  [react/view (styles/numpad-row-container small-screen?)
   [numpad-button a step enabled? small-screen?]
   [numpad-button b step enabled? small-screen?]
   [numpad-button c step enabled? small-screen?]])

(defn numpad
  [step enabled? small-screen?]
  [react/view styles/numpad-container
   [numpad-row [1 2 3] step enabled? small-screen?]
   [numpad-row [4 5 6] step enabled? small-screen?]
   [numpad-row [7 8 9] step enabled? small-screen?]
   [react/view (styles/numpad-row-container small-screen?)
    [react/view (styles/numpad-empty-button small-screen?)]
    [numpad-button 0 step enabled? small-screen?]
    [react/touchable-highlight
     {:on-press #(when enabled?
                   (re-frame/dispatch [:keycard.ui/pin-numpad-delete-button-pressed step]))}
     [react/view (styles/numpad-delete-button small-screen?)
      [icons/icon :main-icons/backspace {:color colors/blue}]]]]])

(defn pin-indicators
  [pin error?]
  [react/view styles/pin-indicator-container
   (map-indexed
    (fn [i n]
      (let [pressed? (number? n)]
        ^{:key i} [react/view (styles/pin-indicator pressed? error?)]))
    (concat pin (repeat (- 6 (count pin)) nil)))])

(defn puk-indicators
  [puk error?]
  [react/view
   {:margin-top      28
    :flex-direction  :row
    :justify-content :space-between}
   (map-indexed
    (fn [i puk-group]
      ^{:key i}
      [react/view
       (merge styles/pin-indicator-container
              {:margin-top 8
               :margin     12})
       (map-indexed
        (fn [j n]
          (if (number? n)
            ^{:key j}
            [react/text
             {:style {:font-size 20
                      :width     18
                      :color     (if error?
                                   colors/red
                                   colors/black)}}
             n]
            ^{:key j} [react/view (styles/puk-indicator error?)]))
        puk-group)])
    (partition 4
               (concat puk
                       (repeat (- 12 (count puk))
                               nil))))])

(defn save-password
  []
  (let [{:keys [save-password?]} @(re-frame/subscribe [:profile/login])
        auth-method              @(re-frame/subscribe [:auth-method])]
    (when-not (and platform/android? (not auth-method))
      [react/view
       {:style {:flex-direction :row}}
       [checkbox/checkbox
        {:checked? save-password?
         :style    {:margin-right 10}}]
       ;; should be reimplemented
       ;;:on-value-change #(re-frame/dispatch [:multiaccounts/save-password %])}]
       [react/text (i18n/label :t/keycard-dont-ask-card)]])))

(defn bezier-easing
  []
  (.bezier ^js animation/easing 0.77 0.000 0.175 1))

(defn animate-info-in
  "animation that makes the error message appear for a few seconds, then
  replaces it with the number of attempts left"
  [error-y-translation error-opacity retries-y-translation retries-opacity]
  (animation/start
   (animation/anim-sequence
    [(animation/parallel
      [(animation/timing error-opacity
                         {:toValue         1
                          :easing          (bezier-easing)
                          :duration        400
                          :useNativeDriver true})
       (animation/timing error-y-translation
                         {:toValue         0
                          :easing          (bezier-easing)
                          :duration        400
                          :useNativeDriver true})])
     (animation/anim-delay 2200)
     (animation/parallel
      [(animation/timing error-opacity
                         {:toValue         0
                          :easing          (bezier-easing)
                          :duration        400
                          :useNativeDriver true})
       (animation/timing error-y-translation
                         {:toValue         8
                          :easing          (bezier-easing)
                          :duration        400
                          :useNativeDriver true})
       (animation/timing retries-opacity
                         {:toValue         1
                          :easing          (bezier-easing)
                          :duration        400
                          :useNativeDriver true})
       (animation/timing retries-y-translation
                         {:toValue         0
                          :easing          (bezier-easing)
                          :duration        400
                          :useNativeDriver true})])])))

(defn animate-info-out
  [retries-y-translation retries-opacity]
  (animation/start
   (animation/parallel
    [(animation/timing retries-opacity
                       {:toValue         0
                        :easing          (bezier-easing)
                        :duration        400
                        :useNativeDriver true})
     (animation/timing retries-y-translation
                       {:toValue         -8
                        :easing          (bezier-easing)
                        :duration        400
                        :useNativeDriver true})])))

(defn pin-view
  [{:keys [retry-counter]}]
  (let [error-y-translation   (animation/create-value -8)
        error-opacity         (animation/create-value 0)
        retries-y-translation (animation/create-value (if retry-counter 0 -8))
        retries-opacity       (animation/create-value (if retry-counter 1 0))
        !error?               (reagent/atom false)]
    (reagent/create-class
     {:component-did-update
      (fn [this [_ previous-props]]
        (let [[_ props]       (.-argv (.-props ^js this))
              previous-status (:status previous-props)
              new-status      (:status props)]
          (case new-status
            :error     (when (or (nil? previous-status)
                                 (= :verifying previous-status))
                         (reset! !error? true)
                         (animate-info-in error-y-translation
                                          error-opacity
                                          retries-y-translation
                                          retries-opacity)
                         (js/setTimeout (fn [] (reset! !error? false)) 3000))
            :verifying (do
                         (animation/set-value error-y-translation -8)
                         (animate-info-out retries-y-translation
                                           retries-opacity))
            nil)))
      :reagent-render
      (fn [{:keys [pin title-label description-label step error-label status
                   retry-counter small-screen? save-password-checkbox?]}]
        (let [enabled? (and (not= status :verifying)
                            (not @!error?))
              puk?     (or (= step :puk) (= step :puk-original) (= step :puk-confirmation))]
          [react/scroll-view
           [react/view styles/pin-container
            [react/view (styles/center-container title-label)
             (when title-label
               [react/text {:style styles/center-title-text}
                (i18n/label title-label)])
             (when description-label
               [react/text
                {:style           styles/create-pin-text
                 :number-of-lines 2}
                (i18n/label description-label)])
             (when save-password-checkbox?
               [save-password])
             [react/view {:style (styles/info-container small-screen?)}
              (when error-label
                [react/animated-view {:style (styles/error-container error-y-translation error-opacity)}
                 [react/text {:style (styles/error-text small-screen?)}
                  (i18n/label error-label)]])
              [react/animated-view
               {:style (styles/retry-container retries-y-translation retries-opacity)}
               (cond
                 (and retry-counter (= retry-counter 1))
                 [react/nested-text
                  {:style {:text-align :center
                           :color      colors/gray}}
                  (i18n/label (if puk?
                                :t/pin-one-attempt-blocked-before
                                :t/pin-one-attempt-frozen-before))
                  [{:style {:color       colors/black
                            :font-weight "700"}}
                   (i18n/label :t/pin-one-attempt)]
                  (i18n/label (if puk?
                                :t/pin-one-attempt-blocked-after
                                :t/pin-one-attempt-frozen-after))]

                 (and retry-counter
                      (< retry-counter
                         (if puk?
                           default-puk-retries-number
                           default-pin-retries-number)))
                 [react/text
                  {:style {:text-align :center
                           :color      colors/gray}}
                  (i18n/label :t/pin-retries-left {:number retry-counter})]
                 :else
                 nil)]]
             (if puk?
               [puk-indicators pin @!error?]
               [pin-indicators pin @!error?])
             [numpad step enabled? small-screen?]]]]))})))

(def pin-retries 3)
(def puk-retries 5)

(defview enter-pin
  []
  (letsubs [pin               [:keycard/pin]
            step              [:keycard/pin-enter-step]
            status            [:keycard/pin-status]
            pin-retry-counter [:keycard/pin-retry-counter]
            puk-retry-counter [:keycard/puk-retry-counter]
            error-label       [:keycard/pin-error-label]]
    (let [;; TODO(rasom): retarded hack to prevent state mess on opening pin
          ;; sheet on another tab and returning back to this screen. Should be
          ;; properly rewritten so that different instances of pin-view do not
          ;; mess with state unrelated to them.
          step (or step :current)]
      [pin-view
       {:pin               pin
        :retry-counter     (if (= step :puk)
                             (when (< puk-retry-counter puk-retries) puk-retry-counter)
                             (when (< pin-retry-counter pin-retries) pin-retry-counter))
        :title-label       (case step
                             :current             :t/current-pin
                             :login               :t/current-pin
                             :import-multiaccount :t/current-pin
                             :original            :t/create-a-pin
                             :confirmation        :t/repeat-pin
                             :puk                 :t/enter-puk-code
                             :puk-original        :t/create-a-puk
                             :puk-confirmation    :t/repeat-puk
                             :t/current-pin)
        :description-label (case step
                             :current             :t/current-pin-description
                             :sign                :t/current-pin-description
                             :import-multiaccount :t/current-pin-description
                             :login               :t/login-pin-description
                             :puk                 :t/enter-puk-code-description
                             :puk-original        :t/new-puk-description
                             :puk-confirmation    :t/new-puk-description
                             :t/new-pin-description)
        :step              step
        :status            status
        :error-label       error-label}])))
