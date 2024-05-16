(ns status-im.contexts.onboarding.generating-keys.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [status-im.common.resources :as resources]
    [status-im.contexts.onboarding.generating-keys.style :as style]
    [utils.i18n :as i18n]))

(def first-title-display-time 3000)
(def second-title-start-time 3500)
(def second-title-display-time 1000)
(def third-title-start-time 5500)
(def transition-duration-time 500)

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

(defn sequence-animation
  [generate-keys-opacity saving-keys-opacity keys-saved-opacity]
  (reanimated/set-shared-value generate-keys-opacity
                               (reanimated/with-delay
                                first-title-display-time
                                (reanimated/with-timing 0
                                                        (js-obj "duration" transition-duration-time
                                                                "easing"   (:linear
                                                                            reanimated/easings)))))
  (reanimated/set-shared-value
   saving-keys-opacity
   (reanimated/with-sequence
    (reanimated/with-delay second-title-start-time
                           (reanimated/with-timing 1
                                                   (js-obj "duration" transition-duration-time
                                                           "easing"   (:linear reanimated/easings))))
    (reanimated/with-delay second-title-display-time
                           (reanimated/with-timing 0
                                                   (js-obj "duration" transition-duration-time
                                                           "easing"   (:linear reanimated/easings))))))
  (reanimated/set-shared-value keys-saved-opacity
                               (reanimated/with-delay
                                third-title-start-time
                                (reanimated/with-timing 1
                                                        (js-obj "duration" transition-duration-time
                                                                "easing"   (:linear
                                                                            reanimated/easings))))))

(defn title
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

(defn content
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

(defn view
  []
  (let [insets                (safe-area/get-insets)
        generate-keys-opacity (reanimated/use-shared-value 1)
        saving-keys-opacity   (reanimated/use-shared-value 0)
        keys-saved-opacity    (reanimated/use-shared-value 0)]
    [rn/view {:style (style/page-container insets)}
     (sequence-animation generate-keys-opacity saving-keys-opacity keys-saved-opacity)
     [:<>
      [title insets generate-keys-opacity saving-keys-opacity keys-saved-opacity]
      [content generate-keys-opacity saving-keys-opacity keys-saved-opacity]]]))
