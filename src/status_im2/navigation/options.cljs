(ns status-im2.navigation.options
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]))

(defn default-options
  []
  {:layout {:orientation ["portrait"]}
   :topBar {:visible false}})

;; Note: Currently, the status bar style provided while setting the root has a high preference,
;; and even if we change the status bar style later dynamically, the style gets restored to this
;; set root style while navigating
;; https://github.com/status-im/status-mobile/pull/15596
(defn statusbar-and-navbar-root
  [& [status-bar-theme]]
  (if platform/android?
    {:navigationBar {:backgroundColor colors/neutral-100}
     :statusBar     {:translucent     true
                     :backgroundColor :transparent
                     :style           (or status-bar-theme :light)
                     :drawBehind      true}}
    {:statusBar {:style (or status-bar-theme :light)}}))

(defn default-root
  [& [status-bar-theme background-color]]
  (merge (statusbar-and-navbar-root status-bar-theme)
         {:topBar {:visible false}
          :layout {:componentBackgroundColor (or background-color
                                                 (colors/theme-colors colors/white colors/neutral-100))
                   :orientation              ["portrait"]
                   :backgroundColor          (or background-color
                                                 (colors/theme-colors colors/white
                                                                      colors/neutral-100))}}))

(def onboarding-layout
  {:componentBackgroundColor colors/neutral-80-opa-80-blur
   :orientation              ["portrait"]
   :backgroundColor          colors/neutral-80-opa-80-blur})

(defn navbar
  ([dark?]
   {:navigationBar {:backgroundColor (if (or dark? (colors/dark?)) colors/neutral-100 colors/white)}})
  ([] (navbar nil)))

(defn statusbar
  ([dark?]
   (let [style (if (or dark? (colors/dark?)) :light :dark)]
     (if platform/android?
       {:statusBar {:translucent     true
                    :backgroundColor :transparent
                    :drawBehind      true
                    :style           style}}
       {:statusBar {:style style}})))
  ([] (statusbar nil)))

(defn statusbar-and-navbar
  ([dark?]
   (merge (navbar dark?) (statusbar dark?)))
  ([] (statusbar-and-navbar nil)))

(defn topbar-options
  []
  {:noBorder             true
   :scrollEdgeAppearance {:active   false
                          :noBorder true}
   :elevation            0
   :title                {:color (colors/theme-colors colors/neutral-100 colors/white)}
   :rightButtonColor     (colors/theme-colors colors/neutral-100 colors/white)
   :background           {:color (colors/theme-colors colors/white colors/neutral-100)}
   :backButton           {:color  (colors/theme-colors colors/neutral-100 colors/white)
                          :testID :back-button}})

(def transparent-screen-options
  (merge
   {:modalPresentationStyle :overCurrentContext
    :layout                 {:componentBackgroundColor :transparent
                             :orientation              ["portrait"]
                             :backgroundColor          :transparent}}
   (if platform/android?
     {:statusBar {:backgroundColor :transparent
                  :style           :light
                  :drawBehind      true}}
     {:statusBar {:style :light}})))

(def sheet-options
  {:layout                 {:componentBackgroundColor :transparent
                            :orientation              ["portrait"]
                            :backgroundColor          :transparent}
   :modalPresentationStyle :overCurrentContext
   :animations             {:showModal    {:alpha {:from 0 :to 1 :duration 300}}
                            :dismissModal {:alpha {:from 1 :to 0 :duration 300}}}})

(def lightbox
  {:topBar        {:visible false}
   :statusBar     {:backgroundColor :transparent
                   :style           :light
                   :animate         true
                   :drawBehind      true
                   :translucent     true}
   :navigationBar {:backgroundColor colors/black}
   :layout        {:componentBackgroundColor :transparent
                   :backgroundColor          :transparent}
   :animations    {:push {:sharedElementTransitions [{:fromId        :shared-element
                                                      :toId          :shared-element
                                                      :interpolation {:type   :decelerate
                                                                      :factor 1.5}}]}
                   :pop  {:sharedElementTransitions [{:fromId        :shared-element
                                                      :toId          :shared-element
                                                      :interpolation {:type
                                                                      :decelerate
                                                                      :factor 1.5}}]}}})

(defn merge-top-bar
  [root-options options]
  (let [options (:topBar options)]
    {:topBar
     (merge root-options
            options
            (when (or (:title root-options) (:title options))
              {:title (merge (:title root-options) (:title options))})
            (when (or (:background root-options) (:background options))
              {:background (merge (:background root-options) (:background options))})
            (when (or (:backButton root-options) (:backButton options))
              {:backButton (merge (:backButton root-options) (:backButton options))})
            (when (or (:leftButtons root-options) (:leftButtons options))
              {:leftButtons (merge (:leftButtons root-options) (:leftButtons options))})
            (when (or (:rightButtons root-options) (:rightButtons options))
              {:rightButtons (merge (:rightButtons root-options) (:rightButtons options))}))}))
