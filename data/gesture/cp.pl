#!/usr/bin/perl

# Expects as input: list of fileneames with complete paths of source filenames
# target folder is hard-coded below

$SALT = "sat_afternoon";

while($line = <STDIN>)
{
   chomp($line);
   print $line;
   $line =~ /(^.+\/)(.+\/)([A-Za-z0-9 _()]+).ins/;
#   print "\n\n1=",$1, " ", $2," ", $3, "\n\n";
# cp source, destination (destination is hard-coded)
   system("cp $1$2$3.ins /Users/ssf/2013-ssf/super-street-fire/data/gesture/ssf_gestures_2013/$2$3_$SALT.ins");

}
