(ns quo.components.links.internal-link-card.view
  (:require [quo.components.links.internal-link-card.channel.view :as channel.view]
            [quo.components.links.internal-link-card.community.view :as community.view]
            [quo.components.links.internal-link-card.user.view :as user.view]))

(defn view
  [{card-type :type :as props}]
  (case card-type
    :community [community.view/view props]
    :channel   [channel.view/view props]
    :user      [user.view/view props]))
