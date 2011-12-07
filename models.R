setwd("C:\\Users\\beladia\\workspace\\lastfm")

train.data <- read.delim(file="traindata_uk1300.dat", header=T, sep="\t")
indx <- sample(1:nrow(train.data), nrow(train.data), replace=F)

train.data <- train.data[indx,]

train.data$connected <- as.factor(train.data$connected)
sample.rows <- sample(1:nrow(train.data), 2/3*nrow(train.data), replace=F)

library(e1071)
svm.model <- svm(connected ~ .,  train.data[sample.rows, c(-1)])
svm.preds <- as.numeric(as.character(predict(svm.model, train.data[-sample.rows,c(-1)])))
tabout <- as.matrix(table(svm.preds, train.data$connected[-sample.rows]))
acc <- (tabout[1] + tabout[4])/sum(tabout)
tpr <- tabout[4]/(tabout[4]+tabout[3])
tnr <- tabout[1]/(tabout[1]+tabout[2])
p <- tabout[4]/(tabout[4]+tabout[2])
r <- tpr
f1 <- 2*r*p/(r+p)


#############################
## Kernel SVM
#############################

library(kernlab)
ksvm.model <- ksvm(connected ~ .,  train.data[sample.rows, c(-1)], kernel="rbfdot")
ksvm.preds <- as.numeric(as.character(predict(ksvm.model, train.data[-sample.rows,c(-1)])))
tabout <- as.matrix(table(ksvm.preds, train.data$connected[-sample.rows]))
acc <- (tabout[1] + tabout[4])/sum(tabout)
tpr <- tabout[4]/(tabout[4]+tabout[3])
tnr <- tabout[1]/(tabout[1]+tabout[2])
p <- tabout[4]/(tabout[4]+tabout[2])
r <- tpr
f1 <- 2*r*p/(r+p)

library(ROCR)
predObject <- prediction(ksvm.preds, train.data$connected[-sample.rows])
perfObject <- performance(predObject, "tpr", "fpr")          
performance(predObject, "auc")@y.values[[1]]


#############################
## Random Forest
#############################

library(randomForest)
library(som)
#train.data[,c(-1,-9,-13)] <- normalize(train.data[,c(-1,-9,-13)])
#for (i in 1:ncol(train.data)){
#  if ((i != 1)  &&  (i != 16))
#    train.data[, i] <- normalize(train.data[, i])
#}

rf.model <- randomForest(connected ~ .,  train.data[sample.rows, c(-1)], ntree=1000)
rf.preds <- as.numeric(as.character(predict(rf.model, train.data[-sample.rows,c(-1)])))
tabout <- as.matrix(table(rf.preds, train.data$connected[-sample.rows]))
acc <- (tabout[1] + tabout[4])/sum(tabout)
tpr <- tabout[4]/(tabout[4]+tabout[3])
tnr <- tabout[1]/(tabout[1]+tabout[2])
p <- tabout[4]/(tabout[4]+tabout[2])
r <- tpr
f1 <- 2*r*p/(r+p)

library(ROCR)
predObject <- prediction(rf.preds, train.data$connected[-sample.rows])
perfObject <- performance(predObject, "tpr", "fpr")          
performance(predObject, "auc")@y.values[[1]]


#############################
## Gradient Boosted Machines
#############################

library(gbm)
weights <- rep(1, length(sample.rows))
weights[which(train.data$connected[sample.rows]==1)] <- 1.1
train.data$connected <- as.character(as.numeric(as.character(train.data$connected)))
gbm.model <- gbm(connected ~ .,  train.data[sample.rows, c(-1)], distribution="bernoulli", shrinkage=0.005, n.trees=10000, weights=weights)
gbm.preds <- predict.gbm(gbm.model, train.data[-sample.rows,c(-1)], 10000, type="response")

gbm.preds[which(gbm.preds > 0.5)] <- 1
gbm.preds[which(gbm.preds <= 0.5)] <- 0

tabout <- as.matrix(table(gbm.preds, train.data$connected[-sample.rows]))
acc <- (tabout[1] + tabout[4])/sum(tabout)
tpr <- tabout[4]/(tabout[4]+tabout[3])
tnr <- tabout[1]/(tabout[1]+tabout[2])
p <- tabout[4]/(tabout[4]+tabout[2])
r <- tpr
f1 <- 2*r*p/(r+p)

library(ROCR)
predObject <- prediction(gbm.preds, train.data$connected[-sample.rows])
perfObject <- performance(predObject, "tpr", "fpr")          
performance(predObject, "auc")@y.values[[1]]


#############################
## Logistic Regression
#############################
train.data$connected <- as.numeric(as.character(train.data$connected))
logr.model <- glm(connected ~ ., train.data[sample.rows, c(-1)], family=binomial("logit"))
logr.preds <- predict(logr.model, train.data[-sample.rows, c(-1)], type="response")
logr.preds[which(logr.preds > 0.5)] <- 1
logr.preds[which(logr.preds <= 0.5)] <- 0

tabout <- as.matrix(table(logr.preds, train.data$connected[-sample.rows]))
acc <- (tabout[1] + tabout[4])/sum(tabout)
tpr <- tabout[4]/(tabout[4]+tabout[3])
tnr <- tabout[1]/(tabout[1]+tabout[2])
p <- tabout[4]/(tabout[4]+tabout[2])
r <- tpr
f1 <- 2*r*p/(r+p)

library(ROCR)
predObject <- prediction(logr.preds, train.data$connected[-sample.rows])
perfObject <- performance(predObject, "tpr", "fpr")          
performance(predObject, "auc")@y.values[[1]]


#############################################
## e1 Ensemble = ksvm, gbm, rf, logr
#############################################
preds <- (ksvm.preds + gbm.preds + rf.preds + logr.preds)/4
preds[which(preds >= 0.5)] <- 1
preds[which(preds < 0.5)] <- 0

tabout <- table(train.data$connected[-sample.rows],preds)   
acc <- (tabout[1] + tabout[4])/sum(tabout)
acc <- (tabout[1] + tabout[4])/sum(tabout)
tpr <- tabout[4]/(tabout[4]+tabout[3])
tnr <- tabout[1]/(tabout[1]+tabout[2])
p <- tabout[4]/(tabout[4]+tabout[2])
r <- tpr
f1 <- 2*r*p/(r+p)



predObject <- prediction(preds, train.data$connected[-sample.rows])
perfObject <- performance(predObject, "tpr", "fpr")          
performance(predObject, "auc")@y.values[[1]]

plot(perfObject, colorize=T)
title("ROC Curve: Ensemble(kernel SVM, Random Forest, GBM, Logistic Regression)", font.main=1)


par(pch=30, col="blue") # plotting symbol and color
par(mfrow=c(4,4)) # all plots on one page


for (i in 1:ncol(train.data)) {
  if ((i != 1) && (i != ncol(train.data))) {
    test <- data.frame(table(x=train.data[train.data$connected==1,i]))
    test$Freq <- log(as.numeric(test$Freq)/sum(as.numeric(test$Freq)))
    test$x <- log(as.numeric(test$x))
    plot(x=test$x, y=test$Freq, col="blue", xlab = names(train.data)[i], ylab="", type="o")
    
    test <- data.frame(table(x=train.data[train.data$connected==0,i]))
    test$Freq <- log(as.numeric(test$Freq)/sum(as.numeric(test$Freq)))
    test$x <- log(as.numeric(test$x))
    points(x=test$x, y=test$Freq, col="red", xlab = names(train.data)[i], ylab="", type="o")
  }
}

legend(x=111, y=690, legend=c("connected","non-conne"), pch=19, col= c(1,0) )



library(corrgram)
corrgram(train.data[,-1], order=NULL, lower.panel=panel.shade,
  upper.panel=NULL, text.panel=panel.txt,
  main="Correlation of Last.fm Data (unsorted)")
