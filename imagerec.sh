#!/bin/bash
input=$1
imgUrl="$(cut -d '/' -f5 <<< "$input")"
source /home/ubuntu/tensorflow/bin/activate
cd /home/ubuntu/tensorflow/models/tutorials/image/imagenet
output=$(python classify_image.py --image_file $1 --num_top_predictions 1)
intOutput="$(cut -d '(' -f1 <<< "$output")"
imgName="$(echo -e "${intOutput}" | sed -e 's/[[:space:]]*$//')"
echo "($imgUrl,$imgName)" > /home/ubuntu/output.txt
