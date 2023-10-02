(ns status-im2.contexts.onboarding.generating-keys.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [react-native.safe-area :as safe-area]
            [status-im2.contexts.onboarding.generating-keys.style :as style]
            [utils.i18n :as i18n]
            [status-im2.common.resources :as resources]
            [status-im2.common.parallax.view :as parallax]
            [status-im2.common.parallax.whitelist :as whitelist]))

(defn generate-keys-title
  []
  [quo/text-combinations
   {:container-style {:margin-horizontal 20}
    :title           (i18n/label :t/generating-keys)}])

(defn saving-keys-title
  []
  [quo/text-combinations
   {:container-style {:margin-horizontal 20}
    :title           (i18n/label :t/saving-keys-to-device)}])

(defn keys-saved-title
  []
  [quo/text-combinations
   {:container-style {:margin-horizontal 20}
    :title           (i18n/label :t/keys-saved)}])

(def first-transition-delay-ms 2000)

(def transition-duration-ms 500)

(defn sequence-animation
  [generate-keys-opacity saving-keys-opacity keys-saved-opacity]
  (reanimated/set-shared-value generate-keys-opacity
                               (reanimated/with-delay
                                first-transition-delay-ms
                                (reanimated/with-timing 0
                                                        (js-obj "duration" transition-duration-ms
                                                                "easing"   (:linear
                                                                            reanimated/easings)))))
  (reanimated/set-shared-value
   saving-keys-opacity
   (reanimated/with-sequence
    (reanimated/with-delay 2000
                           (reanimated/with-timing 1
                                                   (js-obj "duration" transition-duration-ms
                                                           "easing"   (:linear reanimated/easings))))
    (reanimated/with-delay 1000
                           (reanimated/with-timing 0
                                                   (js-obj "duration" transition-duration-ms
                                                           "easing"   (:linear reanimated/easings))))))
  (reanimated/set-shared-value keys-saved-opacity
                               (reanimated/with-delay
                                4600
                                (reanimated/with-timing 1
                                                        (js-obj "duration" transition-duration-ms
                                                                "easing"   (:linear
                                                                            reanimated/easings))))))

(defn f-title
  [insets generate-keys-opacity saving-keys-opacity keys-saved-opacity]
  (let [top-insets (+ (if rn/small-screen? 62 112) (:insets insets))]
    [rn/view
     {:position :absolute
      :top      top-insets}
     [reanimated/view
      {:style (reanimated/apply-animations-to-style
               {:opacity generate-keys-opacity}
               {:position :absolute})}
      [generate-keys-title]]
     [reanimated/view
      {:style (reanimated/apply-animations-to-style
               {:opacity saving-keys-opacity}
               {:position :absolute})}
      [saving-keys-title]]
     [reanimated/view
      {:style (reanimated/apply-animations-to-style
               {:opacity keys-saved-opacity}
               {:position :absolute})}
      [keys-saved-title]]]))

(defn f-page-title
  [insets]
  (let [generate-keys-opacity (reanimated/use-shared-value 1)
        saving-keys-opacity   (reanimated/use-shared-value 0)
        keys-saved-opacity    (reanimated/use-shared-value 0)]
    (sequence-animation generate-keys-opacity saving-keys-opacity keys-saved-opacity)
    [:f> f-title insets generate-keys-opacity saving-keys-opacity keys-saved-opacity]))

(defn f-simple-page-content
  [generate-keys-opacity saving-keys-opacity keys-saved-opacity]
  (let [width (:width (rn/get-window))]
    [rn/view
     {:margin-top 156}
     [reanimated/view
      {:style (reanimated/apply-animations-to-style
               {:opacity generate-keys-opacity}
               {:position :absolute})}
      [rn/image
       {:resize-mode :contain
        :style       (style/page-illustration width)
        :source      (resources/get-image :generate-keys1)}]]

     [reanimated/view
      {:style (reanimated/apply-animations-to-style
               {:opacity saving-keys-opacity}
               {:position :absolute})}
      [rn/image
       {:resize-mode :contain
        :style       (style/page-illustration width)
        :source      (resources/get-image :generate-keys2)}]]
     [reanimated/view
      {:style (reanimated/apply-animations-to-style
               {:opacity keys-saved-opacity}
               {:position :absolute})}
      [rn/image
       {:resize-mode :contain
        :style       (style/page-illustration width)
        :source      (resources/get-image :generate-keys3)}]]]))

(defn parallax-page
  [insets]
  [:<>
   [parallax/video
    {:stretch           -20
     :container-style   {:top  40
                         :left 20}
     :layers            (:generate-keys resources/parallax-video)
     :disable-parallax? true
     :enable-looping?   false}]
   [:f> f-page-title insets]])

(defn f-simple-page
  [insets]
  (let [generate-keys-opacity (reanimated/use-shared-value 1)
        saving-keys-opacity   (reanimated/use-shared-value 0)
        keys-saved-opacity    (reanimated/use-shared-value 0)]
    (sequence-animation generate-keys-opacity saving-keys-opacity keys-saved-opacity)
    [:<>
     [:f> f-title insets generate-keys-opacity saving-keys-opacity keys-saved-opacity]
     [:f> f-simple-page-content generate-keys-opacity saving-keys-opacity keys-saved-opacity]]))

(defn f-generating-keys
  []
  (let [insets (safe-area/get-insets)]
    [rn/view {:style (style/page-container insets)}
     (if whitelist/whitelisted?
       [parallax-page insets]
       [:f> f-simple-page insets])]))

(defn generating-keys
  []
  [:f> f-generating-keys])
