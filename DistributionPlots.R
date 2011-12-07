setwd("C:\\Users\\beladia\\workspace\\lastfm")

par(pch=22, col="blue") # plotting symbol and color
par(mfrow=c(1,3)) # all plots on one page 


# Read the 3 distribution data files

frienddist <- read.delim(file="FriendDistribution_uk1300.dat", header=T, sep="\t")
frienddist <- frienddist[which(!is.na(frienddist[,1])), ]

activitydist <- read.delim(file="ActivityDistribution_uk1300.dat", header=T, sep="\t")
activitydist <- activitydist[which(!is.na(activitydist[,1])), ]

trackdist <- read.delim(file="TrackDistribution_uk1300.dat", header=T, sep="\t")
trackdist <- trackdist[which(!is.na(trackdist[,1])), ]


# % of users with no track info(private playlist)
(trackdist$Count[trackdist$numTracks==0]/sum(trackdist$Count))*100

# Get max users from each data to make Y-axis consistent
max.users <- max(c(frienddist[,2], activitydist[,2], trackdist[,2]))

# round it to nearest multiple of 10
if (max.users %% 10 != 0)
  max.users <- ceiling(max.users/10) * 10


# Plot the distributions
plot(frienddist[,1], frienddist[,2], xlab="# of Friends", ylab="# of Users", ylim=c(0,max.users))
title("Friend Distribution of Last.Fm Users", font.main=1)


plot(activitydist[,1], activitydist[,2], xlab="Range in Days of User Activity", ylab="# of Users", ylim=c(0,max.users))
title("User Activity Distribution of Last.Fm Users", font.main=1)


plot(trackdist[,1], trackdist[,2], xlab="# of Tracks listened", ylab="# of Users", ylim=c(0,max.users))
title("Tracks Distribution of Last.Fm Users", font.main=1)



