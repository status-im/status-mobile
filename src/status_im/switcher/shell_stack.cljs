(ns status-im.switcher.shell-stack
  (:require [status-im.switcher.shell :as shell]
            [status-im.switcher.animation :as animation]
            [status-im.switcher.home-stack :as home-stack]
            [status-im.switcher.bottom-tabs :as bottom-tabs]))

(defn shell-stack []
  [:f>
   (fn []
     (let [shared-values (animation/get-shared-values)]
       [:<>
        [shell/shell]
        [bottom-tabs/bottom-tabs shared-values]
        [home-stack/home-stack shared-values]]))])
