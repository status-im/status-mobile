(ns status-im2.contexts.onboarding.generating-keys1.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [react-native.safe-area :as safe-area]
            [status-im2.contexts.onboarding.generating-keys.style :as style]
            [utils.i18n :as i18n]
            [status-im2.common.resources :as resources]
            [status-im2.common.parallax.view :as parallax]
            [status-im2.contexts.onboarding.common.background.view :as background]))

(defn generate-keys-title
  []
  [quo/title
   {:title (i18n/label :t/generating-keys)}])

(defn saving-keys-title
  []
  [quo/title
   {:title (i18n/label :t/saving-keys-to-device)}])

(defn keys-saved-title
  []
  [quo/title
   {:title (i18n/label :t/keys-saved)}])

(def first-transition-delay 2000)

(def duration 500)

(defn f-page-title
  [insets]
  (let [generate-keys-opacity (reanimated/use-shared-value 1)
        saving-keys-opacity   (reanimated/use-shared-value 0)
        keys-saved-opacity    (reanimated/use-shared-value 0)]

    (reanimated/set-shared-value generate-keys-opacity
                                 (reanimated/with-delay
                                  first-transition-delay
                                  (reanimated/with-timing 0
                                                          (js-obj "duration" duration
                                                                  "easing"   (:linear
                                                                              reanimated/easings)))))
    (reanimated/set-shared-value
     saving-keys-opacity
     (reanimated/with-sequence
      (reanimated/with-delay 2000
                             (reanimated/with-timing 1
                                                     (js-obj "duration" duration
                                                             "easing"   (:linear reanimated/easings))))

      (reanimated/with-delay 1000
                             (reanimated/with-timing 0
                                                     (js-obj "duration" duration
                                                             "easing"   (:linear reanimated/easings))))))
    (reanimated/set-shared-value keys-saved-opacity
                                 (reanimated/with-delay
                                  4600
                                  (reanimated/with-timing 1
                                                          (js-obj "duration" duration
                                                                  "easing"   (:linear
                                                                              reanimated/easings)))))


    [rn/view
     {:position :absolute
      :top      (+ 100 (:insets insets))}

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

(defn f-generating-keys
  [_]
  (let [insets (safe-area/get-insets)]
    [rn/view {:style (style/page-container insets)}
     [background/view true]
     [parallax/video
      {:layers            (resources/get-parallax-video :generate-keys1)
       :disable-parallax? true}]
     [:f> f-page-title insets]]))

(defn generating-keys
  [props]
  [:f> f-generating-keys props])
