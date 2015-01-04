#Last edited by Jordan
#12/9/14

library(XML)

states = as.character(unlist(read.csv(url("http://www.stat.berkeley.edu/users/nolan/data/Project2012/countyVotes2012/stateNames.txt"))))
states = states[-2]
res = "http://www.stat.berkeley.edu/users/nolan/data/Project2012/countyVotes2012/"
xml = list()
for(i in 1:length(states)){
  text = paste(states[i],".xml",sep="")
  url = paste(res,text,sep="")
  xml[[i]] = xmlParse(url)
}
names(xml) = states
states = gsub("-"," ",states)
xml = lapply(xml,xmlRoot)

## COUNTIES
counties = list()
for(i in 1:length(states)){
  counties[[i]] = getNodeSet(xml[[i]],"//th[@class='results-county']")
  names(counties[[i]]) = states[i]
}

for(i in 1:length(counties)){
  current = counties[[i]]
  titles = c(rep("a", length(current)))
  for(x in 2:length(current)){
    titles[x] = xmlValue(current[[x]][[1]])
  }
  counties[[i]] = titles[-1]
}
names(counties) = states

##  PERCENTAGES

percentages = list()
for(i in 1:length(states)){
  percentages[[i]] = getNodeSet(xml[[i]],"//td[@class='results-percentage']")
  names(percentages[[i]]) = states[i]
}

for(i in 1:length(counties)){
  current = percentages[[i]]
  nums = c(rep("a", length(current)))
  for(x in 1:length(current)){
    nums[x] = xmlValue(current[[x]][[1]])
  }
  percentages[[i]] = nums
}

### PARTIES ASSOCIATED WITH THOSE PERCENTAGES
parties = list()
for(i in 1:length(states)){
  parties[[i]] = getNodeSet(xml[[i]],"//td[@class='results-party']")
  names(parties[[i]]) = states[i]
}

for(i in 1:length(parties)){
  current = parties[[i]]
  prty = c(rep("a", length(current)))
  for(x in 1:length(current)){
    prty[x] = xmlValue(current[[x]][[1]])
  }
  parties[[i]] = prty
}
## A LITTLE COMBINATION
combo = list()
for(i in 1:50){
  Party = as.character(parties[[i]])
  Vote = percentages[[i]]
  combo[[i]] = data.frame(Party,Vote,stringsAsFactors = FALSE)
}


combo2 = list()
for(i in 1:50){
  current = subset(combo[[i]], Party=="GOP"| Party == "Dem")
  County = rep(counties[[i]],each = 2)
  State = rep(states[[i]], length(current))
  combo2[[i]] = data.frame(State,County,current,stringsAsFactors = FALSE)
}


FinalCombo = combo2[[1]]
for(i in 2:50){
  FinalCombo = rbind(FinalCombo,combo2[[i]])
}



#  SOURCE #2 (Justin)

race_data= read.csv("http://www.stat.berkeley.edu/users/nolan/data/Project2012/census2010/B01003.csv")
socio_data= read.csv("http://www.stat.berkeley.edu/users/nolan/data/Project2012/census2010/DP02.csv")
economic_data= read.csv("http://www.stat.berkeley.edu/users/nolan/data/Project2012/census2010/DP03.csv")

# Rename all socio and economic data so that no overlap
socio = names(socio_data[-(1:5)])
econ = names(economic_data[-(1:5)])
names(socio_data)[-(1:5)] = paste(socio, "_s", sep = "")
names(economic_data)[-(1:5)] = paste(econ, "_e", sep = "")


race_data$POPGROUP.display.label= as.character(race_data$POPGROUP.display.label)
index_TP= which(race_data$POPGROUP.display.label== "Total population")
Total_Population = race_data$HD01_VD01[index_TP]

race_data$POPGROUP.id= as.character(race_data$POPGROUP.id)
GEO_ID_TP= race_data$GEO.id2[index_TP]

TP_GEO= data.frame(GEO_ID_TP, Total_Population)

index_white= which(race_data$POPGROUP.display.label== "White alone")
white_alone= race_data$HD01_VD01[index_white]
GEO_ID_WHITE= race_data$GEO.id2[index_white]

white_GEO= data.frame(GEO_ID_WHITE, white_alone)

index_black= which(race_data$POPGROUP.id == "4")
black_afam= race_data$HD01_VD01[index_black]
GEO_ID_BLACK= race_data$GEO.id2[index_black]

No_Black=c()
for (i in  1:length(race_data$GEO.display.label)-1) {
  No_Black[i] = race_data$GEO.display.label[i] == race_data$GEO.display.label[i+1]
}

Which_No_Black = which(No_Black == FALSE)

Black_Pop= c(NA)
for (i in 2:length(Which_No_Black)) {
  if (Which_No_Black[i]- Which_No_Black[i-1] == 2){
    Black_Pop[i] = as.integer(race_data$GEO.id2)[Which_No_Black[i]]
  }else {
    Black_Pop[i] = NA
  }
}
Black_Pop = Black_Pop[!is.na(Black_Pop)]

Black_Geo_ID = append(GEO_ID_BLACK, Black_Pop)
Black_Pop_Values = append(black_afam, rep(NA, length = length(Black_Pop)))
Black_Pop_Values = Black_Pop_Values[order(Black_Geo_ID)]

black_GEO= data.frame(Black_Geo_ID, Black_Pop_Values)

TP_WH= merge(TP_GEO, white_GEO, by.x="GEO_ID_TP", by.y="GEO_ID_WHITE")

TP_WH_BL= merge(TP_WH, black_GEO, by.x="GEO_ID_TP", by.y="Black_Geo_ID")

merge_race_socio= merge(TP_WH_BL, socio_data, by.x="GEO_ID_TP", by.y= "GEO.id2")

merge_all= merge(merge_race_socio, economic_data, by.x="GEO_ID_TP", by.y="GEO.id2")

merge_all= merge_all[, -c(5,7,8)]
merge_all= merge_all[, c(1,5,2,3,4,6:757)]


## Forming FinalCombo into a more agreeable format

dem = subset(FinalCombo, Party == "Dem")
gop = subset(FinalCombo, Party == "GOP")
gop = gop[-1:-3]
FinalCombo = data.frame(dem,gop)
FinalCombo = FinalCombo[-3]
names(FinalCombo) = c("State", "County", "Dem Vote", "GOP Vote")
for(i in 1:3113){
  FinalCombo[i,2] = gsub(" $","", FinalCombo[i,2])
}

## SOURCE #3

gml= xmlParse("http://www.stat.berkeley.edu/users/nolan/data/Project2012/counties.gml")
getNodeSet(gml, "//state")
groot= xmlRoot(gml)
state_names= xpathSApply(groot,"//state/gml:name", xmlValue)


state_names= gsub("\n   ", "", state_names)

tdata = xmlToList(gml)
counties = list()
test = for(i in 1:51){
  state = tdata[[i]][[1]][[1]]
  state = gsub("\n   ", "", state)
  current = rep(0,length(tdata[[i]]))
  for(x in 2:length(tdata[[i]])){
    current[[x]] = tdata[[i]][[x]][[1]]
  }
  counties[[i]] = current[-1]
  names(counties[i])=state
}
start = data.frame()
for(i in 1:51){
  County = gsub("\n   ", "", counties[[i]])
  State = rep(state_names[i],length(counties[[i]]))
  add = data.frame(State,County,stringsAsFactors = FALSE)
  start = rbind(start, add)
}

# state_names = xpathSApply(groot, "//") <----- Do We need this???

location= xpathSApply(groot, "//county/gml:coord")
getNodeSet(gml, "//gml:X")
x_coord= xpathSApply(groot, "//gml:X", xmlValue)
x_coord= gsub("\n   ", "", x_coord)
y_coord= xpathSApply(groot, "//gml:Y", xmlValue)
y_coord = gsub("\n  ", "", y_coord)

coordinates_df= data.frame(start, x_coord, y_coord,stringsAsFactors = FALSE)
coordinates_df = coordinates_df[-68:-92,]
for(i in 1:nrow(coordinates_df)){
  coordinates_df[i,2] = gsub(" County","", coordinates_df[i,2])
  coordinates_df[i,2] = gsub(" ","", coordinates_df[i,2])
}
names(coordinates_df) = c("State", "County", "X Coordinate", "Y Coordinate")


for(i in 1:nrow(coordinates_df)){
  coordinates_df[i,1] = gsub(" ","", coordinates_df[i,1])
}
for(i in 1:nrow(FinalCombo)){
  FinalCombo[i,1] = gsub("-"," ",FinalCombo[i,1])
  FinalCombo[i,2] = gsub(" ","",FinalCombo[i,2])
  FinalCombo[i,1] = gsub(" ","",FinalCombo[i,1])
}
for(i in 1:nrow(FinalCombo)){
  FinalCombo[i,2] = paste(FinalCombo[i,1],FinalCombo[i,2])
}

for(i in 1:nrow(coordinates_df)){
  coordinates_df[i,2] = paste(tolower(coordinates_df[i,1]),coordinates_df[i,2])
}
FinalCombo = FinalCombo[-1]
coordinates_df = coordinates_df[-1]


for(i in 2886:2926){
  coordinates_df[i,1] = gsub("city","",coordinates_df[i,1])
}


for(i in 1:nrow(coordinates_df)){
  coordinates_df[i,1] = gsub("Parish","",coordinates_df[i,1])
}

FinalCombo[,1] = tolower(FinalCombo[,1])
coordinates_df[,1] = tolower(coordinates_df[,1])
FinalCombo[546,1] = "idaho idaho"
FinalCombo[1167,1] = "maryland baltimore"
FinalCombo[1403,1] = "mississippi jeffersondavis"
resultsandcoords = merge(FinalCombo,coordinates_df)

new_names = c()                       
for  ( i in 1:nrow(merge_all)) {
  new_names[i]= gsub(" County" , "", as.character(merge_all$GEO.display.label.x)[i])
}

for  ( i in 1:nrow(merge_all)) {
  new_names[i]= gsub(" Parish" , "", as.character(new_names)[i])
}

for  ( i in 1:nrow(merge_all)) {
  new_names[i]= gsub(" city" , "", as.character(new_names)[i])
}

new_names = tolower(new_names)

this=list()
for (i in 1:length(new_names)) {
  this[i] = strsplit(new_names[i], ", ")
}

switchcountystate = list()
for (i in 1:length(new_names)) {
  switchcountystate[[i]] = gsub(" ", "", as.vector(this[[i]]))
}

newvector = c()
for (i in 1:length(new_names)){
  newvector[i] = paste(switchcountystate[[i]][2], switchcountystate[[i]][1])
}

merge_all$GEO.display.label.x = newvector
merge_all= merge(resultsandcoords, merge_all,  by.y="GEO.display.label.x", by.x="County")

final_data_frame = merge_all
final_data_frame$County = gsub("\\.","",final_data_frame$County)
final_data_frame$County = gsub(",","",final_data_frame$County)

### PART 3 ###
library(rpart)
library(class)

# Collect 2004 data and merge with final_data_frame
data_2004 = read.table(url("http://www.stat.berkeley.edu/~nolan/data/Project2012/countyVotes2004.txt"),header = TRUE,stringsAsFactors = FALSE)
data_2004 = data.frame(rep("a",2975),data_2004,stringsAsFactors = FALSE)
names(data_2004) = c("State", "County", "GOP_Vote_2004", "Dem_Vote_2004")

for(i in 1:length(data_2004$County)){
  current = data_2004$County[i]
  data_2004[i,1] = gsub(",.*","",data_2004$County[i])
  data_2004[i,2] = gsub(".*,","",data_2004$County[i])
}


data_2004$County = gsub(" ", "", data_2004$County)
data_2004$State = gsub(" ", "", data_2004$State)
data_2004$County = paste(data_2004$State, data_2004$County)
data_2004$County[283] = "districtofcolumbia districtofcolumbia"
data_2004 = data_2004[,-1]
FINAL = merge(data_2004, final_data_frame)



# Clean up
colnames(FINAL)[4] = "Dem_Vote_2012"
colnames(FINAL)[5] = "GOP_Vote_2012"
FINAL$Dem_Vote_2004 = as.numeric(FINAL$Dem_Vote_2004)
FINAL$GOP_Vote_2004 = as.numeric(FINAL$GOP_Vote_2004)
FINAL$Dem_Vote_2012 = as.numeric(gsub("%", "", FINAL$Dem_Vote_2012))
FINAL$GOP_Vote_2012 = as.numeric(gsub("%", "", FINAL$GOP_Vote_2012))
colnames(FINAL)[6] = "x_coord"
colnames(FINAL)[7] = "y_coord"


# Create vector containing winners of each county. 0 is GOP. 1 is Dem

winner = function(GOP, Dem) {
  if (Dem > GOP) {
    return("Dem")
  }
  return("GOP")
}

results_2004 = c()
results_2012 = c()
for (i in 1:nrow(FINAL)) {
  results_2004 = c(results_2004, winner(FINAL$GOP_Vote_2004[i], FINAL$Dem_Vote_2004[i]))
  results_2012 = c(results_2012, winner(FINAL$GOP_Vote_2012[i], FINAL$Dem_Vote_2012[i]))
}

FINAL = cbind(results_2004, results_2012, FINAL)
FINAL = FINAL[c(3, 1, 2, 4:ncol(FINAL))]

write.table(FINAL, "data.txt", sep="\t")

# Use recursive partitioning to predict 2012 results using 2004 training set with variables below

# RACE/Other
# GeoTag
geo = FINAL$GEO_ID_TP
# Total Population
pop = FINAL$Total_Population
# White Alone
white = FINAL$white_alone
# Black Population
black = FINAL$Black_Pop_Values

# Percentage of White People
white_perc = (white/pop) * 100

# X Coordinates
x = as.numeric(FINAL$x_coord)
# Y Coordinates
y = as.numeric(FINAL$y_coord)


# SOCIO

# HC03_VC04 - Percent; HOUSEHOLDS BY TYPE - family households (families)
families = FINAL$HC03_VC04_s

# HC03_VC12,"Percent; HOUSEHOLDS BY TYPE - Family households (families) - Female householder, no husband 
# present, family - With own children under 18 years"
single_mom = FINAL$HC03_VC12_s

# HC03_VC13,Percent; HOUSEHOLDS BY TYPE - Nonfamily households
nonfamilies = FINAL$HC03_VC13_s

# HC03_VC15,Percent; HOUSEHOLDS BY TYPE - Nonfamily households - Householder living alone - 65 years and over
elderly = FINAL$HC03_VC15_s

# HC03_VC25,Percent; RELATIONSHIP - Population in households
#family_size = FINAL$HC03_VC25_s
#class(family_size)
#family_size

#HC03_VC42,Percent; MARITAL STATUS - Females 15 years and over
females= FINAL$HC03_VC42_s

# Percentage of Females
females_perc = (females/pop) * 100

# HC03_VC87,Percent; EDUCATIONAL ATTAINMENT - High school graduate (includes equivalency)
high_school_grads = FINAL$HC03_VC13_s

# HC03_VC130,Percent; PLACE OF BIRTH - Native - Born in United States
us_born = FINAL$HC03_VC130_s

# HC03_VC139,Percent; U.S. CITIZENSHIP STATUS - Naturalized U.S. citizen
#naturalized = FINAL$HC03_VC139_s
#class(naturalized)
#naturalized= as.numeric(as.character(naturalized))

# HC03_VC171,Percent; LANGUAGE SPOKEN AT HOME - Language other than English - Spanish
spanish_speaking = FINAL$HC03_VC171_s


# ECON

# HC03_VC13,Percent; EMPLOYMENT STATUS - Percent Unemployed
unemployed = FINAL$HC03_VC13_e
# HC03_VC84,"Percent; INCOME AND BENEFITS (IN 2010 INFLATION-ADJUSTED DOLLARS) - $200,000 or more"
rich = FINAL$HC03_VC84_e

# HC01_VC112,Estimate; INCOME AND BENEFITS (IN 2010 INFLATION-ADJUSTED DOLLARS) - Median family income (dollars)
median_family_income = FINAL$HC01_VC112_e

# HC03_VC166,Percent; PERCENTAGE OF FAMILIES AND PEOPLE WHOSE INCOME IN THE PAST 12 MONTHS IS # BELOW THE POVERTY LEVEL - All people
poor = FINAL$HC03_VC166_e


### NOT WORKING ###
# HC03_VC139,Percent; HEALTH INSURANCE COVERAGE - In labor force: - Employed: - With health insurance 
# coverage
# insured = FINAL$HC03_VC139_e
# HC03_VC146,Percent; HEALTH INSURANCE COVERAGE - In labor force: - Unemployed: - With health insurance 
# coverage - With public coverage
# uninsured_workers = FINAL$HC03_VC146_e

a = rpart(results_2004 ~ pop + white_perc + families + single_mom + nonfamilies + elderly + 
            females_perc + high_school_grads + us_born + spanish_speaking + 
            unemployed + rich + median_family_income + poor, 
          method = 'class')
print(a)
summary(a)
plot(a, uniform = TRUE)
text(a, all=T, cex=.5)

test = predict(a)
win = c()
for(i in 1:2956){
  if(test[i,1]>test[i,2]){win[i] = "Dem"}
  if(test[i,1]<test[i,2]){win[i] = "GOP"}
}
sum(knn_results == results_2012)/length(results_2012)



#---------------------------------------------
# kNN
x = x/1000000
y = y/1000000
variables= data.frame(x , y , pop, white_perc, families, single_mom, nonfamilies, 
                      elderly, females_perc, high_school_grads, us_born, 
                      spanish_speaking, unemployed, rich, median_family_income, poor)

#variables = variables[-which(is.na(naturalized)),]

#v = list(x_coord, y_coord, pop, white_perc, families, single_mom, nonfamilies, 
#      elderly, females_perc, high_school_grads, us_born, naturalized, 
#      spanish_speaking, unemployed, rich, median_family_income, poor)
#which(is.na(v))

cl = results_2004
test = variables
train = variables
knn_results = knn(train, test, cl, k = 2)
sum(knn_results == results_2012)/length(results_2012)

length(which(results_2004 == "GOP"))/length(results_2004)
length(which(results_2012 == "GOP"))/length(results_2012)
length(which(knn_results == "GOP"))/length(knn_results)



knn_error = function(x) {
  knn_results = knn(train, test, cl, k = x)
  return(1 - (sum(knn_results == results_2012)/length(results_2012)))
}

k = 1:30
percent_true = sapply(k, knn_error)*100
plot(percent_true ~ k, xlab = "K-values", ylab = "Misclassification Rate")




### NYT PLOT
library(maps)
delta = data.frame()
for(i in 1:nrow(FINAL)){
  p04r = (FINAL[i,4]*100)/(FINAL[i,4]+FINAL[i,5])
  p12r = FINAL[i,7]
  delta[i,1] = FINAL[i,1]
  delta[i,2] = round(p12r - p04r,3)
  if(delta[i,2] < 0){delta[i,3] = "blue"}
  if(delta[i,2] > 0){delta[i,3] = "red"}
  delta[i,4] = as.numeric(FINAL[i,8])/1000000
  delta[i,5] = as.numeric(FINAL[i,9])/1000000
}
names(delta) = c("county","change","result","x","y")
png("New York Times.png",width = 1280,height=800)
map('usa')
for(i in 1:nrow(delta)){
  scale = if((delta[i,2] * (1/10)) < 0){(delta[i,2] * (1/10))-.25}else((delta[i,2] * (1/10))+.25)
  arrows(delta[i,4],delta[i,5],delta[i,4]+scale,delta[i,5]+scale, col = delta[i,3],length = .05)
}
dev.off()


## income rate plot
means = FINAL$HC01_VC86_e
adds = data.frame(means,results_2012,stringsAsFactors = FALSE)
q1 = adds[means<=48000,]
q2 = adds[means>=48000 & means<= 54000,]
q3 = adds[means>=54000 & means<= 61000,]
q4 = adds[means>=61000 & means<= 138000,]

pq1 = sum(q1$results_2012 == "GOP")/nrow(q1)
pq2 = sum(q2$results_2012 == "GOP")/nrow(q2)
pq3 = sum(q3$results_2012 == "GOP")/nrow(q3)
pq4 = sum(q4$results_2012 == "GOP")/nrow(q4)
nums = c(pq1,pq2,pq3,pq4)
png("Percentage of counties GOP by Income.png",width = 800,height=800)
barplot(nums,names.arg = c("<48000","48000 to 54000","54000 to 61000","61000 to 138000"),
        main = "Percentage of Counties with a GOP Result in Different Mean Incomes",
        xlab = "Mean Income Level in 2010 inflation-adjusted dollars",
        ylab = "Percentage with GOP result",
        ylim = 0:1)

dev.off()


## pop plot

pops = FINAL$Total_Population
adds2 = data.frame(pops,results_2012,stringsAsFactors = FALSE)
qq1 = adds2[pops<=11110,]
qq2 = adds2[pops>=11110 & pops<= 25950,]
qq3 = adds2[pops>=25950 & pops<= 66560,]
qq4 = adds2[pops>=66560 & pops<= 9759000,]

pqq1 = sum(qq1$results_2012 == "GOP")/nrow(qq1)
pqq2 = sum(qq2$results_2012 == "GOP")/nrow(qq2)
pqq3 = sum(qq3$results_2012 == "GOP")/nrow(qq3)
pqq4 = sum(qq4$results_2012 == "GOP")/nrow(qq4)
nums = c(pqq1,pqq2,pqq3,pqq4)
png("Percentage of counties GOP by Population.png",width = 800,height=800)
barplot(nums,names.arg = c("<11110","11110 to 25950","25950 to 66560","66560 to 9759000"),
        main = "Percentage of Counties with a GOP Result in Different Population Sizes",
        xlab = "Population",
        ylab = "Percentage with GOP result",
        ylim = 0:1)

dev.off()

#Unemployment Histograms

dem_unemp = unemployed[which(FINAL$results_2012 == "Dem")]
rep_unemp = unemployed[which(FINAL$results_2012 == "GOP")]
avg_dem_unemp = mean(dem_unemp)
avg_rep_unemp = mean(rep_unemp)
sd_dem_unemp = sd(dem_unemp)
sd_rep_unemp = sd(rep_unemp)
png("Distribution of Dem Counties by Unemployment Rate.png",width = 800,height=800)
hist(dem_unemp, breaks = 100, xlim = c(0,30), ylab = "Frequency", xlab = "Unemployment", main = "Democratic County Unemployment")
arrows(mean(dem_unemp), 40, mean(dem_unemp)+3,40)
text(mean(dem_unemp)+8, 40, "Average = 8.849; SD = 3.8")
dev.off()
png("Distribution of GOP Counties by Unemployment Rate.png",width = 800,height=800)
hist(rep_unemp, breaks = 100, xlim = c(0,30), ylab = "Frequency", xlab = "Unemployment", main = "Republican County Unemployment")
arrows(mean(rep_unemp), 65, mean(rep_unemp)+3,65)
text(mean(rep_unemp)+8, 65, "Average = 7.185; SD = 3.01")
dev.off()


