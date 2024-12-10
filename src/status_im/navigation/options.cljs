(ns status-im.navigation.options
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]
    [status-im.navigation.transitions :as transitions]))

(defn default-options
  []
  {:layout {:orientation ["portrait"]}
   :topBar {:visible false}})

(defn statusbar-and-navbar-options
  [theme status-bar-theme nav-bar-color]
  (let [[status-bar-theme nav-bar-color]
        (if (= :dark theme)
          [(or status-bar-theme :light) (or nav-bar-color colors/neutral-100)]
          [(or status-bar-theme :dark) (or nav-bar-color colors/white)])]
    (if platform/android?
      {:navigationBar {:backgroundColor nav-bar-color}
       :statusBar     {:translucent     true
                       :backgroundColor :transparent
                       :style           (or status-bar-theme :light)
                       :drawBehind      true}}
      {:statusBar {:style (or status-bar-theme :light)}})))

(defn root-options
  [{:keys [background-color theme status-bar-theme nav-bar-color]}]
  (let [layout-background    (or background-color
                                 (colors/theme-colors colors/white colors/neutral-100 theme))
        component-background (or background-color
                                 (colors/theme-colors colors/white colors/neutral-100 theme))]
    (assoc (statusbar-and-navbar-options theme status-bar-theme nav-bar-color)
           :topBar {:visible false}
           :layout {:componentBackgroundColor component-background
                    :orientation              ["portrait"]
                    :backgroundColor          layout-background})))

(defn dark-root-options
  []
  (root-options
   {:background-color colors/neutral-100
    :theme            :dark
    :status-bar-theme :light
    :nav-bar-color    colors/neutral-100}))

;;;; Screen Specific Options
(def onboarding-layout
  {:componentBackgroundColor colors/neutral-80-opa-80-blur
   :orientation              ["portrait"]
   :backgroundColor          colors/neutral-80-opa-80-blur})

(def onboarding-transparent-layout
  {:componentBackgroundColor :transparent
   :orientation              ["portrait"]
   :backgroundColor          :transparent})

(def transparent-screen-options
  {:modalPresentationStyle :overCurrentContext
   :theme                  :dark
   :layout                 {:componentBackgroundColor :transparent
                            :orientation              ["portrait"]
                            :backgroundColor          :transparent}})

(def transparent-modal-screen-options
  (merge
   transparent-screen-options
   {:animations (merge
                 transitions/new-to-status-modal-animations
                 transitions/push-animations-for-transparent-background)}))

(def sheet-options
  {:layout                 {:componentBackgroundColor :transparent
                            :orientation              ["portrait"]
                            :backgroundColor          :transparent}
   :modalPresentationStyle :overCurrentContext
   ;; disabled on iOS in debug mode:
   ;; https://github.com/status-im/status-mobile/pull/16053#issuecomment-1568349702
   :animations             (if (or platform/android? (not js/goog.DEBUG))
                             {:showModal    {:alpha {:from 1 :to 1 :duration 300}}
                              :dismissModal {:alpha {:from 1 :to 1 :duration 300}}}
                             {})})

(def dark-screen
  {:theme                  :dark
   :modalPresentationStyle :overCurrentContext
   :layout                 {:componentBackgroundColor colors/neutral-95
                            :orientation              ["portrait"]
                            :backgroundColor          colors/neutral-95}})

(def lightbox
  {:topBar        {:visible false}
   :statusBar     {:backgroundColor :transparent
                   :style           :light
                   :animate         true
                   :drawBehind      true
                   :translucent     true}
   :navigationBar {:backgroundColor colors/neutral-100}
   :layout        {:componentBackgroundColor :transparent
                   :backgroundColor          :transparent
                   ;; issue: https://github.com/wix/react-native-navigation/issues/7726
                   :orientation              (if platform/ios? ["portrait" "landscape"] ["portrait"])}
   :animations    {:push {:sharedElementTransitions [{:fromId        :shared-element
                                                      :toId          :shared-element
                                                      :interpolation {:type   :decelerate
                                                                      :factor 1.5}}]}
                   :pop  {:sharedElementTransitions [{:fromId        :shared-element
                                                      :toId          :shared-element
                                                      :interpolation {:type
                                                                      :decelerate
                                                                      :factor 1.5}}]}}})
