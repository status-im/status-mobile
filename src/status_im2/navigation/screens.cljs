(ns status-im2.navigation.screens
  (:require [status-im2.setup.config :as config]
            [status-im.ui2.screens.quo2-preview.main :as quo2.preview]
            [status-im.ui2.screens.communities.discover-communities :as discover-communities]

            ;; TODO (14/11/22 flexsurfer) move to status-im2 namespace
            [status-im.switcher.shell-stack :as shell-stack]
            [status-im.ui.screens.screens :as old-screens]))

(def components
  [])

(defn screens []
  (concat [{:name      :shell-stack
            :insets    {:top false}
            :component shell-stack/shell-stack}

           {:name      :discover-communities
            ;;TODO animated-header scroll behaviours
            :options   {:topBar {:visible false}}
            :component discover-communities/communities}]
          (old-screens/screens)
          (when config/quo-preview-enabled?
            quo2.preview/screens)
          (when config/quo-preview-enabled?
            quo2.preview/main-screens)))
